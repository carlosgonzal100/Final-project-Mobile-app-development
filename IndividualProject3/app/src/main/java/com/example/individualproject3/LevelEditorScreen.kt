package com.example.individualproject3

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tile-based level editor used by the parent to design custom dungeons.
 *
 * Features:
 * - Choose grid size (4x4 up to 14x14)
 * - Paint tiles from a palette (walls, water, floor, corners)
 * - Place start and goal positions (hero + door)
 * - Save as custom level for later use
 * - Export the layout as Kotlin code to "bake" into createAllLevels()
 */

// ---------- Logical meaning for gameplay (how tiles behave) ----------

enum class LogicalTileType {
    EMPTY,
    FLOOR,
    WALL,
    WATER
}

/**
 * Simple test container for previewing levels (used by dev tools elsewhere).
 */
data class EditorTestLevel(
    val width: Int,
    val height: Int,
    val tileIds: List<List<String>>,
    val startPos: Pair<Int, Int>,
    val goalPos: Pair<Int, Int>,
    val difficulty: Difficulty = Difficulty.EASY
)

/**
 * A single selectable tile in the palette.
 *
 * @param id          stable ID used in tileIds
 * @param displayName short label shown in the palette
 * @param resId       drawable resource ID
 * @param logicalType how this tile behaves in gameplay (floor/wall/water/empty)
 */
data class PaletteTile(
    val id: String,
    val displayName: String,
    val resId: Int,
    val logicalType: LogicalTileType
)

/**
 * Simple definition used when saving a custom level from the editor.
 * Stored as IDs only; gameplay collision (walls/water) is derived from palette.
 */
data class CustomLevelDef(
    val id: String,
    val difficulty: Difficulty,
    val width: Int,
    val height: Int,
    val tileIds: List<List<String>>
)

/**
 * Save a custom level to a text file using a compact pipe-separated format.
 * This is a legacy saver (you now primarily use SavedCustomLevel + JSON),
 * but kept here because it’s still referenced by older code paths.
 */
fun saveCustomLevel(
    context: Context,
    def: CustomLevelDef,
    palette: List<PaletteTile>,
    startPos: Pair<Int, Int>?,
    goalPos: Pair<Int, Int>?
) {
    val paletteById = palette.associateBy { it.id }

    val walls = mutableListOf<Pair<Int, Int>>()
    val water = mutableListOf<Pair<Int, Int>>()

    // Derive wall + water coordinates from logical tile type
    for (y in 0 until def.height) {
        for (x in 0 until def.width) {
            val id = def.tileIds[y][x]
            val pal = paletteById[id] ?: continue
            when (pal.logicalType) {
                LogicalTileType.WALL  -> walls += x to y
                LogicalTileType.WATER -> water += x to y
                else -> {}
            }
        }
    }

    val startX = startPos?.first ?: -1
    val startY = startPos?.second ?: -1
    val goalX = goalPos?.first ?: -1
    val goalY = goalPos?.second ?: -1

    fun encodeList(list: List<Pair<Int, Int>>): String =
        list.joinToString(";") { "${it.first}:${it.second}" }

    // Flatten everything into a single line
    val line = buildString {
        append(def.id).append('|')
        append(def.difficulty.name).append('|')
        append(def.width).append('|')
        append(def.height).append('|')
        append("$startX,$startY").append('|')
        append("$goalX,$goalY").append('|')
        append(encodeList(walls)).append('|')
        append(encodeList(water)).append('|')

        // Flatten tile IDs so you can reconstruct visuals 1:1 later
        def.tileIds.forEachIndexed { y, row ->
            row.forEachIndexed { x, id ->
                append(id)
                if (!(y == def.height - 1 && x == def.width - 1)) append(',')
            }
        }
    }

    context.openFileOutput("custom_levels.txt", Context.MODE_APPEND).use { fos ->
        fos.write((line + "\n").toByteArray())
    }
}

// ---------- LEVEL EDITOR UI ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelEditorScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // --- PALETTE: Every placeable tile type in the editor ---
    val palette: List<PaletteTile> = listOf(
        // Base floor and obstacles
        PaletteTile("floor", "Floor", R.drawable.floor_tile, LogicalTileType.FLOOR),
        PaletteTile("inner_wall", "Inner Wall", R.drawable.inner_wall, LogicalTileType.WALL),
        PaletteTile("water", "Water", R.drawable.water_tile, LogicalTileType.WATER),

        // Side walls (upper / lower)
        PaletteTile("left_upper", "Left U", R.drawable.left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("left_lower", "Left L", R.drawable.left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("right_upper", "Right U", R.drawable.right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("right_lower", "Right L", R.drawable.right_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("top_upper", "Top U", R.drawable.top_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("top_lower", "Top L", R.drawable.top_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bottom_upper", "Bottom U", R.drawable.bottom_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bottom_lower", "Bottom L", R.drawable.bottom_side_lower_wall, LogicalTileType.WALL),

        // Corner lower layer
        PaletteTile("tl_lower", "TL L", R.drawable.top_left_corner_lower_wall, LogicalTileType.WALL),
        PaletteTile("tr_lower", "TR L", R.drawable.top_right_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bl_lower", "BL L", R.drawable.bottom_left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("br_lower", "BR L", R.drawable.bottom_right_side_lower_wall, LogicalTileType.WALL),

        // Corner upper layer
        PaletteTile("tl_upper", "TL U", R.drawable.top_left_corner_upper_wall, LogicalTileType.WALL),
        PaletteTile("tr_upper", "TR U", R.drawable.top_right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bl_upper", "BL U", R.drawable.bottom_left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("br_upper", "BR U", R.drawable.bottom_right_side_upper_wall, LogicalTileType.WALL),

        // Outer big corners (for corridor-style rooms)
        PaletteTile("outer_tl", "Outer TL", R.drawable.outer_top_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_tr", "Outer TR", R.drawable.outer_top_right_corner, LogicalTileType.WALL),
        PaletteTile("outer_bl", "Outer BL", R.drawable.outer_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_br", "Outer BR", R.drawable.outer_bottom_right_corner, LogicalTileType.WALL),

        // Inner corners (for carving shapes inside rooms/corridors)
        PaletteTile("inner_tl", "Inner TL", R.drawable.inner_top_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_tr", "Inner TR", R.drawable.inner_top_right_corner, LogicalTileType.WALL),
        PaletteTile("inner_bl", "Inner BL", R.drawable.inner_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_br", "Inner BR", R.drawable.inner_bottom_right_corner, LogicalTileType.WALL)
    )

    val paletteById = remember { palette.associateBy { it.id } }

    // Sprites used for start (hero) and goal overlay
    val heroResId = R.drawable.down_sprite
    val goalResId = R.drawable.goal

    // --- Editor meta state ---

    var levelId by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf(Difficulty.EASY) }

    var gridSize by remember { mutableStateOf(10) }
    var tileIds by remember {
        mutableStateOf(List(10) { List(10) { "empty" } })
    }

    var startPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var goalPos by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // selectedMode can be:
    //  "tile:<id>" → paint that tile
    //  "start"     → place hero start
    //  "goal"      → place goal
    //  "erase"     → clear to empty
    var selectedMode by remember { mutableStateOf("tile:floor") }

    var statusMessage by remember { mutableStateOf("") }

    // Holds the generated Kotlin code after "Export as Kotlin"
    var exportCode by remember { mutableStateOf("") }

    // Reset grid when changing size
    fun resetGrid(newSize: Int) {
        gridSize = newSize
        tileIds = List(newSize) { List(newSize) { "empty" } }
        startPos = null
        goalPos = null
        statusMessage = ""
        exportCode = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Level Editor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- Meta controls (ID, difficulty, grid size) ----------

            OutlinedTextField(
                value = levelId,
                onValueChange = { levelId = it },
                label = { Text("Level ID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Difficulty:")
                Difficulty.values().forEach { diff ->
                    FilterChip(
                        selected = (difficulty == diff),
                        onClick = { difficulty = diff },
                        label = { Text(diff.name) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Grid: $gridSize x $gridSize")
                Slider(
                    value = gridSize.toFloat(),
                    onValueChange = { v ->
                        val s = v.toInt().coerceIn(4, 14)
                        if (s != gridSize) resetGrid(s)
                    },
                    valueRange = 4f..14f,
                    steps = 10,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Tile palette row ----------

            Text("Tile Palette", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                palette.forEach { tile ->
                    val modeId = "tile:${tile.id}"
                    val isSelected = selectedMode == modeId

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(4.dp)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.Yellow else Color.DarkGray
                            )
                            .clickable { selectedMode = modeId }
                            .padding(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = tile.resId),
                            contentDescription = tile.displayName,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = tile.displayName,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ---------- Special modes: Start, Goal, Eraser ----------

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val startSelected = selectedMode == "start"
                OutlinedButton(
                    onClick = { selectedMode = "start" },
                    border = if (startSelected)
                        ButtonDefaults.outlinedButtonBorder.copy(width = 3.dp)
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Image(
                        painter = painterResource(id = heroResId),
                        contentDescription = "Start",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Start")
                }

                val goalSelected = selectedMode == "goal"
                OutlinedButton(
                    onClick = { selectedMode = "goal" },
                    border = if (goalSelected)
                        ButtonDefaults.outlinedButtonBorder.copy(width = 3.dp)
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Image(
                        painter = painterResource(id = goalResId),
                        contentDescription = "Goal",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Goal")
                }

                val eraseSelected = selectedMode == "erase"
                OutlinedButton(
                    onClick = { selectedMode = "erase" },
                    border = if (eraseSelected)
                        ButtonDefaults.outlinedButtonBorder.copy(width = 3.dp)
                    else
                        ButtonDefaults.outlinedButtonBorder
                ) {
                    Text("Eraser")
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Pick a tile or mode, then tap cells.\n" +
                        "Start/Goal are sprites on top of tiles.\n" +
                        "Eraser clears a cell to empty.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // ---------- Grid: paintable tile area ----------

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111111))
                    .padding(4.dp)
            ) {
                // Scale tile size so the whole grid fits the width
                val tileSize = maxWidth / gridSize

                Column {
                    for (y in 0 until gridSize) {
                        Row {
                            for (x in 0 until gridSize) {
                                val id = tileIds[y][x]
                                val baseTile = paletteById[id]

                                Box(
                                    modifier = Modifier
                                        .size(tileSize)
                                        .border(1.dp, Color.DarkGray)
                                        .clickable {
                                            when {
                                                selectedMode == "erase" -> {
                                                    val newRows = tileIds
                                                        .map { it.toMutableList() }
                                                        .toMutableList()
                                                    newRows[y][x] = "empty"
                                                    tileIds = newRows.map { it.toList() }
                                                }

                                                selectedMode == "start" -> {
                                                    startPos = x to y
                                                }

                                                selectedMode == "goal" -> {
                                                    goalPos = x to y
                                                }

                                                selectedMode.startsWith("tile:") -> {
                                                    val tileId = selectedMode.removePrefix("tile:")
                                                    val newRows = tileIds
                                                        .map { it.toMutableList() }
                                                        .toMutableList()
                                                    newRows[y][x] = tileId
                                                    tileIds = newRows.map { it.toList() }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Base tile art
                                    if (baseTile != null) {
                                        Image(
                                            painter = painterResource(id = baseTile.resId),
                                            contentDescription = baseTile.displayName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.FillBounds
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black)
                                        )
                                    }

                                    // Overlays: start + goal
                                    if (startPos?.first == x && startPos?.second == y) {
                                        Image(
                                            painter = painterResource(id = heroResId),
                                            contentDescription = "Start Sprite",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    if (goalPos?.first == x && goalPos?.second == y) {
                                        Image(
                                            painter = painterResource(id = goalResId),
                                            contentDescription = "Goal Sprite",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- BUTTONS: Test (stub), Save, Export ----------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // TEST LEVEL: currently just a stub (dev-only)
                Button(
                    onClick = {
                        statusMessage = "Test Level is disabled for now (dev-only feature)."
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Level")
                }

                // SAVE LEVEL: writes a SavedCustomLevel via CustomLevels.kt utilities
                Button(
                    onClick = {
                        if (startPos == null || goalPos == null) {
                            statusMessage = "You must place a Start and a Goal before saving."
                        } else {
                            val safeId = levelId.ifBlank { "custom_level_${System.currentTimeMillis()}" }

                            val saved = SavedCustomLevel(
                                id = safeId,
                                difficulty = difficulty,
                                width = gridSize,
                                height = gridSize,
                                startX = startPos!!.first,
                                startY = startPos!!.second,
                                goalX = goalPos!!.first,
                                goalY = goalPos!!.second,
                                tileIds = tileIds
                            )

                            saveCustomLevelToFile(context, saved)
                            statusMessage = "Saved level \"$safeId\""
                            exportCode = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Level")
                }
            }

            Spacer(Modifier.height(8.dp))

            // EXPORT: take a saved custom level and convert it into Kotlin
            Button(
                onClick = {
                    val targetId = levelId.ifBlank { "custom_level_1" }

                    val allCustom = loadCustomLevels(context)
                    val saved = allCustom.find { it.id == targetId }

                    if (saved != null) {
                        val code = exportSavedLevelAsKotlin(saved, varName = "easy1Tiles")
                        exportCode = code
                        statusMessage =
                            "Exported code for \"$targetId\". Scroll down to copy it."
                    } else {
                        statusMessage = "No saved custom level with id \"$targetId\""
                        exportCode = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export as Kotlin (dev)")
            }

            Spacer(Modifier.height(8.dp))

            // Status banner (errors / success messages)
            if (statusMessage.isNotEmpty()) {
                Text(
                    text = statusMessage,
                    color = if (
                        statusMessage.startsWith("Saved") ||
                        statusMessage.startsWith("Exported")
                    ) Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Show exported Kotlin block if present
            if (exportCode.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Exported Kotlin (copy and paste into createAllLevels):",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 250.dp)
                        .border(1.dp, Color.Gray)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = exportCode,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
