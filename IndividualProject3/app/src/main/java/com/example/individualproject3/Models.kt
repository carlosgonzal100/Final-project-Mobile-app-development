package com.example.individualproject3

enum class Command {
    MOVE_UP, MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT
}

enum class HeroFacing {
    UP, DOWN, LEFT, RIGHT
}

enum class Difficulty {
    EASY, HARD
}

data class GameMap(
    val id: String,
    val width: Int,
    val height: Int,
    val startX: Int,
    val startY: Int,
    val goalX: Int,
    val goalY: Int,
    val walls: Set<Pair<Int, Int>>,
    val waterTiles: Set<Pair<Int, Int>> = emptySet(),
    // NEW: full visual tile grid from the editor (optional for old built-in levels)
    val tileIds: List<List<String>>? = null
)

data class Level(
    val id: String,
    val name: String,
    val difficulty: Difficulty,
    val games: List<GameMap>
)

data class AttemptLog(
    val childId: String,
    val levelId: String,
    val gameId: String,
    val success: Boolean,
    val movesUsed: Int,
    val timestampMillis: Long
)

data class ParentAccount(
    val id: String,
    val name: String,
    val pin: String
)

data class ChildAccount(
    val id: String,
    val name: String,
    val age: Int? = null,
    val notes: String? = null,
    val parentId: String
)
