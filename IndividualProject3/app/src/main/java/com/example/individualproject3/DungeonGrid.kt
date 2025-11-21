package com.example.individualproject3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

//----the dungeon grid ----------//
//helps with positioning and visuals for the grid
@Composable
fun DungeonGrid(
    gameMap: GameMap,
    heroPos: Pair<Int, Int>,
    heroFacing: HeroFacing,
    heroShake: Pair<Int, Int> = 0 to 0,
    heroSinkProgress: Float = 0f
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val tileSize: Dp = maxWidth / gameMap.width

        // --- Hero sprites --- //
        val heroPainter: Painter = when (heroFacing) {
            HeroFacing.UP    -> painterResource(R.drawable.up_sprite)
            HeroFacing.DOWN  -> painterResource(R.drawable.down_sprite)
            HeroFacing.LEFT  -> painterResource(R.drawable.left_sprite)
            HeroFacing.RIGHT -> painterResource(R.drawable.right_sprite)
        }

        val maxX = gameMap.width - 1
        val maxY = gameMap.height - 1

        // If we have a full tile grid from the editor, use it for 1:1 visuals.
        val tiles = gameMap.tileIds
        if (tiles != null) {
            // Match editor palette IDs -> painters
            val painterById: Map<String, Painter> = mapOf(
                "floor"      to painterResource(R.drawable.floor_tile),
                "inner_wall" to painterResource(R.drawable.inner_wall),
                "water"      to painterResource(R.drawable.water_tile),

                "left_upper"   to painterResource(R.drawable.left_side_upper_wall),
                "left_lower"   to painterResource(R.drawable.left_side_lower_wall),
                "right_upper"  to painterResource(R.drawable.right_side_upper_wall),
                "right_lower"  to painterResource(R.drawable.right_side_lower_wall),

                "top_upper"    to painterResource(R.drawable.top_side_upper_wall),
                "top_lower"    to painterResource(R.drawable.top_side_lower_wall),
                "bottom_upper" to painterResource(R.drawable.bottom_side_upper_wall),
                "bottom_lower" to painterResource(R.drawable.bottom_side_lower_wall),

                "tl_lower"     to painterResource(R.drawable.top_left_corner_lower_wall),
                "tr_lower"     to painterResource(R.drawable.top_right_side_lower_wall),
                "bl_lower"     to painterResource(R.drawable.bottom_left_side_lower_wall),
                "br_lower"     to painterResource(R.drawable.bottom_right_side_lower_wall),

                "tl_upper"     to painterResource(R.drawable.top_left_corner_upper_wall),
                "tr_upper"     to painterResource(R.drawable.top_right_side_upper_wall),
                "bl_upper"     to painterResource(R.drawable.bottom_left_side_upper_wall),
                "br_upper"     to painterResource(R.drawable.bottom_right_side_upper_wall),

                "outer_tl"     to painterResource(R.drawable.outer_top_left_corner),
                "outer_tr"     to painterResource(R.drawable.outer_top_right_corner),
                "outer_bl"     to painterResource(R.drawable.outer_bottom_left_corner),
                "outer_br"     to painterResource(R.drawable.outer_bottom_right_corner),

                "inner_tl"     to painterResource(R.drawable.inner_top_left_corner),
                "inner_tr"     to painterResource(R.drawable.inner_top_right_corner),
                "inner_bl"     to painterResource(R.drawable.inner_bottom_left_corner),
                "inner_br"     to painterResource(R.drawable.inner_bottom_right_corner)
            )

            val doorGoal = painterResource(R.drawable.goal)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (y in 0 until gameMap.height) {
                    Row {
                        for (x in 0 until gameMap.width) {
                            val isHero = (heroPos.first == x && heroPos.second == y)
                            val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                            val id = tiles[y][x]
                            val basePainter: Painter? = painterById[id]

                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                if (basePainter != null) {
                                    Image(
                                        painter = basePainter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.FillBounds
                                    )
                                } else {
                                    // Unknown / "empty" -> pure black
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                    )
                                }

                                // Goal overlay
                                if (isGoal) {
                                    Image(
                                        painter = doorGoal,
                                        contentDescription = "Goal",
                                        modifier = Modifier.fillMaxSize(0.85f),
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Hero overlay (with shake + sink)
                                if (isHero) {
                                    Image(
                                        painter = heroPainter,
                                        contentDescription = "Hero",
                                        modifier = Modifier
                                            .offset(
                                                x = heroShake.first.dp,
                                                y = heroShake.second.dp
                                            )
                                            .graphicsLayer(
                                                scaleX = 1f - heroSinkProgress * 0.6f,
                                                scaleY = 1f - heroSinkProgress * 0.6f,
                                                alpha  = 1f - heroSinkProgress
                                            )
                                            .fillMaxSize(0.8f),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ---------- FALLBACK: old arena/room visuals for built-in levels ----------
            val floorTile   = painterResource(R.drawable.floor_tile)
            val waterTile   = painterResource(R.drawable.water_tile)
            val innerWall   = painterResource(R.drawable.inner_wall)

            // Outer wall tiles (upper ring)
            val topSideUpper            = painterResource(R.drawable.top_side_upper_wall)
            val bottomSideUpper         = painterResource(R.drawable.bottom_side_upper_wall)
            val leftSideUpper           = painterResource(R.drawable.left_side_upper_wall)
            val rightSideUpper          = painterResource(R.drawable.right_side_upper_wall)
            val topLeftUpperCorner      = painterResource(R.drawable.top_left_corner_upper_wall)
            val topRightUpperCorner     = painterResource(R.drawable.top_right_side_upper_wall)
            val bottomLeftUpperCorner   = painterResource(R.drawable.bottom_left_side_upper_wall)
            val bottomRightUpperCorner  = painterResource(R.drawable.bottom_right_side_upper_wall)

            // Outer wall tiles (lower ring)
            val topSideLower            = painterResource(R.drawable.top_side_lower_wall)
            val bottomSideLower         = painterResource(R.drawable.bottom_side_lower_wall)
            val leftSideLower           = painterResource(R.drawable.left_side_lower_wall)
            val rightSideLower          = painterResource(R.drawable.right_side_lower_wall)
            val topLeftLowerCorner      = painterResource(R.drawable.top_left_corner_lower_wall)
            val topRightLowerCorner     = painterResource(R.drawable.top_right_side_lower_wall)
            val bottomLeftLowerCorner   = painterResource(R.drawable.bottom_left_side_lower_wall)
            val bottomRightLowerCorner  = painterResource(R.drawable.bottom_right_side_lower_wall)

            val doorGoal = painterResource(R.drawable.goal)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (y in 0 until gameMap.height) {
                    Row {
                        for (x in 0 until gameMap.width) {
                            val isHero = (heroPos.first == x && heroPos.second == y)
                            val isGoal = (gameMap.goalX == x && gameMap.goalY == y)

                            val basePainter: Painter = when {
                                // Outer wall: upper ring
                                isUpperWallRing(x, y, gameMap) -> {
                                    when {
                                        x == 0 && y == 0 -> topLeftUpperCorner
                                        x == maxX && y == 0 -> topRightUpperCorner
                                        x == 0 && y == maxY -> bottomLeftUpperCorner
                                        x == maxX && y == maxY -> bottomRightUpperCorner
                                        y == 0 -> topSideUpper
                                        y == maxY -> bottomSideUpper
                                        x == 0 -> leftSideUpper
                                        x == maxX -> rightSideUpper
                                        else -> topSideUpper
                                    }
                                }

                                // Outer wall: lower ring
                                isLowerWallRing(x, y, gameMap) -> {
                                    when {
                                        x == 1 && y == 1 -> topLeftLowerCorner
                                        x == maxX - 1 && y == 1 -> topRightLowerCorner
                                        x == 1 && y == maxY - 1 -> bottomLeftLowerCorner
                                        x == maxX - 1 && y == maxY - 1 -> bottomRightLowerCorner
                                        y == 1 -> topSideLower
                                        y == maxY - 1 -> bottomSideLower
                                        x == 1 -> leftSideLower
                                        x == maxX - 1 -> rightSideLower
                                        else -> topSideLower
                                    }
                                }

                                // Water & inner walls
                                gameMap.waterTiles.contains(x to y) -> waterTile
                                gameMap.walls.contains(x to y) -> innerWall

                                // Goal and floor
                                isGoal -> doorGoal
                                else -> floorTile
                            }

                            Box(
                                modifier = Modifier
                                    .size(tileSize)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = basePainter,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.FillBounds
                                )

                                if (isHero) {
                                    Image(
                                        painter = heroPainter,
                                        contentDescription = "Hero",
                                        modifier = Modifier
                                            .offset(
                                                x = heroShake.first.dp,
                                                y = heroShake.second.dp
                                            )
                                            .graphicsLayer(
                                                scaleX = 1f - heroSinkProgress * 0.6f,
                                                scaleY = 1f - heroSinkProgress * 0.6f,
                                                alpha  = 1f - heroSinkProgress
                                            )
                                            .fillMaxSize(0.8f),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Two-tile-thick outer wall around the map
fun isUpperWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 0 || x == maxX || y == 0 || y == maxY)
}

fun isLowerWallRing(x: Int, y: Int, map: GameMap): Boolean {
    val maxX = map.width - 1
    val maxY = map.height - 1
    return (x == 1 || x == maxX - 1 || y == 1 || y == maxY - 1)
}

// For movement checks later
fun isOuterWall(x: Int, y: Int, map: GameMap): Boolean =
    isUpperWallRing(x, y, map) || isLowerWallRing(x, y, map)

