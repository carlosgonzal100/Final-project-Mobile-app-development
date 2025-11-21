package com.example.individualproject3

import android.content.Context

// Handles on-device storage of user-created levels and mapping them onto game slots.

// One saved custom level on disk
data class SavedCustomLevel(
    val id: String,
    val difficulty: Difficulty,
    val width: Int,
    val height: Int,
    val startX: Int,
    val startY: Int,
    val goalX: Int,
    val goalY: Int,
    val tileIds: List<List<String>>  // same IDs you use in the editor
)

// Where we store custom levels on the device
private const val CUSTOM_LEVELS_FILE = "custom_levels.txt"

// Where we store which custom is applied to which game slot
// Lines like: easy_game1|my_corridor_level
private const val APPLIED_LEVELS_FILE = "applied_custom_levels.txt"

// ----------------------
// Encode / decode helpers for custom levels
// ----------------------

private fun encodeLevelToLine(level: SavedCustomLevel): String {
    val flatTiles = level.tileIds.flatten().joinToString(",")
    return buildString {
        append(level.id).append('|')                       // 0
        append(level.difficulty.name).append('|')          // 1
        append(level.width).append('|')                    // 2
        append(level.height).append('|')                   // 3
        append(level.startX).append(',').append(level.startY).append('|') // 4
        append(level.goalX).append(',').append(level.goalY).append('|')   // 5
        append(flatTiles)                                  // 6
    }
}

private fun decodeLevelFromLine(line: String): SavedCustomLevel? {
    if (line.isBlank()) return null
    val parts = line.split('|')
    if (parts.size < 7) return null

    val id = parts[0]
    val difficulty = try {
        Difficulty.valueOf(parts[1])
    } catch (e: Exception) {
        Difficulty.EASY
    }

    val width = parts[2].toIntOrNull() ?: return null
    val height = parts[3].toIntOrNull() ?: return null

    val startParts = parts[4].split(',')
    val goalParts = parts[5].split(',')
    if (startParts.size != 2 || goalParts.size != 2) return null

    val startX = startParts[0].toIntOrNull() ?: return null
    val startY = startParts[1].toIntOrNull() ?: return null
    val goalX = goalParts[0].toIntOrNull() ?: return null
    val goalY = goalParts[1].toIntOrNull() ?: return null

    val tilesFlat = parts[6].split(',')
    val expectedSize = width * height
    if (tilesFlat.size < expectedSize) return null

    val tileIds2d = (0 until height).map { y ->
        (0 until width).map { x ->
            tilesFlat[y * width + x]
        }
    }

    return SavedCustomLevel(
        id = id,
        difficulty = difficulty,
        width = width,
        height = height,
        startX = startX,
        startY = startY,
        goalX = goalX,
        goalY = goalY,
        tileIds = tileIds2d
    )
}

// ----------------------
// Public API: load & save custom levels
// ----------------------

fun loadCustomLevels(context: Context): List<SavedCustomLevel> {
    return try {
        val text = context.openFileInput(CUSTOM_LEVELS_FILE)
            .bufferedReader()
            .use { it.readText() }

        text.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { decodeLevelFromLine(it) }
            .toList()
    } catch (e: Exception) {
        emptyList()
    }
}

// Save/update one custom level.
// If a level with the same id already exists, it gets replaced.
// Otherwise it's appended.
fun saveCustomLevelToFile(context: Context, level: SavedCustomLevel) {
    val existing = loadCustomLevels(context).toMutableList()

    val index = existing.indexOfFirst { it.id == level.id }
    if (index >= 0) {
        existing[index] = level
    } else {
        existing.add(level)
    }

    val allLines = existing.joinToString("\n") { encodeLevelToLine(it) }

    context.openFileOutput(CUSTOM_LEVELS_FILE, Context.MODE_PRIVATE).use { fos ->
        fos.write(allLines.toByteArray())
    }
}

// Optional: delete a custom level by id (does NOT touch defaults)
fun deleteCustomLevel(context: Context, id: String) {
    val existing = loadCustomLevels(context).toMutableList()
    val newList = existing.filter { it.id != id }

    val allLines = newList.joinToString("\n") { encodeLevelToLine(it) }

    context.openFileOutput(CUSTOM_LEVELS_FILE, Context.MODE_PRIVATE).use { fos ->
        fos.write(allLines.toByteArray())
    }

    // Also clear any applied mapping that points to this custom
    val mappings = loadAppliedMappings(context).toMutableMap()
    val keysToRemove = mappings.filterValues { it == id }.keys
    keysToRemove.forEach { mappings.remove(it) }
    saveAppliedMappings(context, mappings)
}

// ----------------------
// Applied custom → which game slot uses which custom
// ----------------------

// gameId -> customLevelId
fun loadAppliedMappings(context: Context): Map<String, String> {
    return try {
        val text = context.openFileInput(APPLIED_LEVELS_FILE)
            .bufferedReader()
            .use { it.readText() }

        text.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split('|')
                if (parts.size == 2) {
                    val gameId = parts[0]
                    val customId = parts[1]
                    gameId to customId
                } else null
            }
            .toMap()
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun saveAppliedMappings(context: Context, mapping: Map<String, String>) {
    val lines = mapping.entries.joinToString("\n") { (gameId, customId) ->
        "$gameId|$customId"
    }
    context.openFileOutput(APPLIED_LEVELS_FILE, Context.MODE_PRIVATE).use { fos ->
        fos.write(lines.toByteArray())
    }
}

// Set (or change) which custom level is used for a game slot
fun applyCustomToGame(context: Context, gameId: String, customLevelId: String) {
    val existing = loadAppliedMappings(context).toMutableMap()
    existing[gameId] = customLevelId
    saveAppliedMappings(context, existing)
}

// Remove custom from a game slot → goes back to default map
fun clearCustomFromGame(context: Context, gameId: String) {
    val existing = loadAppliedMappings(context).toMutableMap()
    existing.remove(gameId)
    saveAppliedMappings(context, existing)
}

// ----------------------
// Convert SavedCustomLevel → GameMap for gameplay
// ----------------------

// Map a tile id string from the editor to a logical type used by gameplay.
fun logicalTypeForTileId(tileId: String): LogicalTileType =
    when (tileId) {
        // Water stays water
        "water" -> LogicalTileType.WATER

        // Any kind of wall piece
        "inner_wall",
        "left_upper", "left_lower",
        "right_upper", "right_lower",
        "top_upper", "top_lower",
        "bottom_upper", "bottom_lower",
        "tl_lower", "tr_lower", "bl_lower", "br_lower",
        "tl_upper", "tr_upper", "bl_upper", "br_upper",
        "outer_tl", "outer_tr", "outer_bl", "outer_br",
        "inner_tl", "inner_tr", "inner_bl", "inner_br" ->
            LogicalTileType.WALL

        // True walkable tiles
        "floor" -> LogicalTileType.FLOOR

        // Black / not painted = NOT walkable (so corridor stays narrow)
        "empty", "" -> LogicalTileType.WALL

        // Anything unknown, be safe and block it
        else -> LogicalTileType.WALL
    }

// Make a GameMap that GameScreen + DungeonGrid can play
fun SavedCustomLevel.toGameMap(idOverride: String? = null): GameMap {
    val walls = mutableSetOf<Pair<Int, Int>>()
    val water = mutableSetOf<Pair<Int, Int>>()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val type = logicalTypeForTileId(tileIds[y][x])
            when (type) {
                LogicalTileType.WALL -> walls += x to y
                LogicalTileType.WATER -> water += x to y
                else -> {}
            }
        }
    }

    return GameMap(
        id = idOverride ?: id,
        width = width,
        height = height,
        startX = startX,
        startY = startY,
        goalX = goalX,
        goalY = goalY,
        walls = walls,
        waterTiles = water,
        tileIds = tileIds          // used for 1:1 visual rendering in DungeonGrid
    )
}

// Helper for baked-in defaults built from a 2D list of tile ids.
fun gameMapFromTileIds(
    id: String,
    startX: Int,
    startY: Int,
    goalX: Int,
    goalY: Int,
    tileIds: List<List<String>>
): GameMap {
    val height = tileIds.size
    val width = tileIds.firstOrNull()?.size ?: 0

    val walls = mutableSetOf<Pair<Int, Int>>()
    val water = mutableSetOf<Pair<Int, Int>>()

    for (y in 0 until height) {
        for (x in 0 until width) {
            val t = logicalTypeForTileId(tileIds[y][x])
            when (t) {
                LogicalTileType.WALL -> walls += x to y
                LogicalTileType.WATER -> water += x to y
                else -> {}
            }
        }
    }

    return GameMap(
        id = id,
        width = width,
        height = height,
        startX = startX,
        startY = startY,
        goalX = goalX,
        goalY = goalY,
        walls = walls,
        waterTiles = water,
        tileIds = tileIds
    )
}

// Export a saved custom level as ready-to-paste Kotlin code (used for "baking in" levels).
fun exportSavedLevelAsKotlin(level: SavedCustomLevel, varName: String = "levelTiles"): String {
    val sb = StringBuilder()
    sb.append("val ").append(varName).append(" = listOf(\n")

    for (y in 0 until level.height) {
        sb.append("    listOf(")
        val row = level.tileIds[y]
        row.forEachIndexed { x, id ->
            sb.append("\"").append(id).append("\"")
            if (x != row.size - 1) sb.append(", ")
        }
        sb.append(")")
        if (y != level.height - 1) sb.append(",")
        sb.append("\n")
    }

    sb.append(")\n\n")
    sb.append("val game = gameMapFromTileIds(\n")
    sb.append("    id = \"").append(level.id).append("\",\n")
    sb.append("    startX = ").append(level.startX).append(",\n")
    sb.append("    startY = ").append(level.startY).append(",\n")
    sb.append("    goalX = ").append(level.goalX).append(",\n")
    sb.append("    goalY = ").append(level.goalY).append(",\n")
    sb.append("    tileIds = ").append(varName).append("\n")
    sb.append(")\n")

    return sb.toString()
}
