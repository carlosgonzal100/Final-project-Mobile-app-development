package com.example.individualproject3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Read-only preview of a level from the editor.
 * Shows tiles + start/goal exactly as they were drawn in the grid.
 */

/**this file will eventually be used at some point in the future to
 * test levels right from the editor instead of applying it to a custom
 * level. the custom levels are for the developer only as of now
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorTestScreen(
    testLevel: EditorTestLevel,
    onBack: () -> Unit
) {
    // Local palette used only for visual preview (same IDs as editor)
    val palette: List<PaletteTile> = listOf(
        PaletteTile("floor", "Floor", R.drawable.floor_tile, LogicalTileType.FLOOR),
        PaletteTile("inner_wall", "Inner Wall", R.drawable.inner_wall, LogicalTileType.WALL),
        PaletteTile("water", "Water", R.drawable.water_tile, LogicalTileType.WATER),

        PaletteTile("left_upper", "Left U", R.drawable.left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("left_lower", "Left L", R.drawable.left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("right_upper", "Right U", R.drawable.right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("right_lower", "Right L", R.drawable.right_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("top_upper", "Top U", R.drawable.top_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("top_lower", "Top L", R.drawable.top_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bottom_upper", "Bottom U", R.drawable.bottom_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bottom_lower", "Bottom L", R.drawable.bottom_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("tl_lower", "TL L", R.drawable.top_left_corner_lower_wall, LogicalTileType.WALL),
        PaletteTile("tr_lower", "TR L", R.drawable.top_right_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("bl_lower", "BL L", R.drawable.bottom_left_side_lower_wall, LogicalTileType.WALL),
        PaletteTile("br_lower", "BR L", R.drawable.bottom_right_side_lower_wall, LogicalTileType.WALL),

        PaletteTile("tl_upper", "TL U", R.drawable.top_left_corner_upper_wall, LogicalTileType.WALL),
        PaletteTile("tr_upper", "TR U", R.drawable.top_right_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("bl_upper", "BL U", R.drawable.bottom_left_side_upper_wall, LogicalTileType.WALL),
        PaletteTile("br_upper", "BR U", R.drawable.bottom_right_side_upper_wall, LogicalTileType.WALL),

        PaletteTile("outer_tl", "Outer TL", R.drawable.outer_top_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_tr", "Outer TR", R.drawable.outer_top_right_corner, LogicalTileType.WALL),
        PaletteTile("outer_bl", "Outer BL", R.drawable.outer_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("outer_br", "Outer BR", R.drawable.outer_bottom_right_corner, LogicalTileType.WALL),

        PaletteTile("inner_tl", "Inner TL", R.drawable.inner_top_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_tr", "Inner TR", R.drawable.inner_top_right_corner, LogicalTileType.WALL),
        PaletteTile("inner_bl", "Inner BL", R.drawable.inner_bottom_left_corner, LogicalTileType.WALL),
        PaletteTile("inner_br", "Inner BR", R.drawable.inner_bottom_right_corner, LogicalTileType.WALL)
    )

    val paletteById = remember { palette.associateBy { it.id } }

    val heroResId = R.drawable.down_sprite
    val goalResId = R.drawable.goal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test Level View") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Preview of your level exactly as you drew it.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF111111))
                    .padding(4.dp)
            ) {
                Column {
                    for (y in 0 until testLevel.height) {
                        Row {
                            for (x in 0 until testLevel.width) {
                                val id = testLevel.tileIds[y][x]
                                val baseTile = paletteById[id]

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(1.dp, Color.DarkGray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Base tile image or black if unknown/empty
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

                                    // Start hero overlay
                                    if (testLevel.startPos.first == x && testLevel.startPos.second == y) {
                                        Image(
                                            painter = painterResource(id = heroResId),
                                            contentDescription = "Start",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    // Goal overlay
                                    if (testLevel.goalPos.first == x && testLevel.goalPos.second == y) {
                                        Image(
                                            painter = painterResource(id = goalResId),
                                            contentDescription = "Goal",
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

            Spacer(Modifier.height(12.dp))
            Text(
                "Use this to verify your tile layout & corners.\n" +
                        "Gameplay (commands / sliding) is still in GameScreen.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
