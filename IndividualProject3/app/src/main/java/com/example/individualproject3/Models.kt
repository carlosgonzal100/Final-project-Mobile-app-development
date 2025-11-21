package com.example.individualproject3

/**
 * High-level commands the child can drag into the program.
 * Each one represents a direction the hero will slide in.
 */
enum class Command {
    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT
}

/**
 * Current facing direction of the hero, used only for choosing
 * which sprite (up/down/left/right) to draw.
 */
enum class HeroFacing {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

/**
 * Difficulty bucket for levels and custom levels.
 */
enum class Difficulty {
    EASY,
    HARD
}

/**
 * A single playable map inside a Level.
 *
 * Coordinate system:
 *  - (0, 0) is the top-left tile.
 *  - x increases to the right.
 *  - y increases downward.
 *
 * @param walls      set of coordinates that are solid walls (inner walls / obstacles)
 * @param waterTiles set of coordinates that behave as water (instant fail on contact)
 * @param tileIds    optional full visual grid from the editor;
 *                   if present, DungeonGrid will render using these IDs 1:1.
 */
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
    // When null, built-in levels use auto-generated walls around the edges.
    val tileIds: List<List<String>>? = null
)

/**
 * A group of maps shown under a single difficulty label
 * (e.g., "Easy Dungeons" with 3 mini-games).
 */
data class Level(
    val id: String,
    val name: String,
    val difficulty: Difficulty,
    val games: List<GameMap>
)

/**
 * Parent account that logs in with a PIN and manages children.
 */
data class ParentAccount(
    val id: String,
    val name: String,
    val pin: String
)

/**
 * A child profile belonging to a single parent. Progress stats in the CSV
 * are linked using the child's name.
 */
data class ChildAccount(
    val id: String,
    val name: String,
    val age: Int? = null,
    val notes: String? = null,
    val parentId: String
)
