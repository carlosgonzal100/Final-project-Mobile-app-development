package com.example.individualproject3

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PlayCustomLevelDialog(
    customLevels: List<SavedCustomLevel>,
    onDismiss: () -> Unit,
    onPlay: (SavedCustomLevel) -> Unit
) {
    var selected by remember { mutableStateOf<SavedCustomLevel?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose a custom level") },
        text = {
            Column {
                Text(
                    "Tap a custom level to select it:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    customLevels.forEach { lvl ->
                        val isSelected = selected?.id == lvl.id
                        OutlinedButton(
                            onClick = { selected = lvl },
                            modifier = Modifier.fillMaxWidth(),
                            border = if (isSelected)
                                ButtonDefaults.outlinedButtonBorder.copy(width = 3.dp)
                            else
                                ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text("${lvl.id} (${lvl.difficulty}, ${lvl.width}x${lvl.height})")
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "The custom level you pick will be played right away.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = (selected != null),
                onClick = {
                    val chosen = selected ?: return@TextButton
                    onPlay(chosen)
                }
            ) {
                Text("Play")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    levels: List<Level>,
    onBack: () -> Unit,
    onSelectGame: (Level, GameMap) -> Unit
) {
    val context = LocalContext.current

    var customLevels by remember { mutableStateOf<List<SavedCustomLevel>>(emptyList()) }
    var showCustomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        customLevels = loadCustomLevels(context)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = DungeonBg,
        topBar = {
            TopAppBar(
                title = { Text("Select Dungeon", color = DungeonTextMain) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", color = DungeonTextMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DungeonPanel,
                    titleContentColor = DungeonTextMain
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DungeonBg)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            levels.forEach { level ->
                Text(
                    "${level.name} (${level.difficulty})",
                    style = MaterialTheme.typography.titleMedium,
                    color = DungeonTextMain
                )
                Spacer(Modifier.height(8.dp))

                level.games.forEach { game ->
                    Button(
                        onClick = { onSelectGame(level, game) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DungeonPanel,
                            contentColor = DungeonTextMain
                        )
                    ) {
                        Text("Play: ${game.id}")
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = DungeonBorder)
            Spacer(Modifier.height(8.dp))

            Text("Custom Levels", style = MaterialTheme.typography.titleMedium, color = DungeonTextMain)
            Spacer(Modifier.height(8.dp))

            if (customLevels.isEmpty()) {
                Text(
                    "No custom levels saved yet.\nUse the Level Editor to forge your own dungeon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DungeonTextSub
                )
            } else {
                Button(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DungeonAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Play Custom Level")
                }
            }
        }

        if (showCustomDialog) {
            PlayCustomLevelDialog(
                customLevels = customLevels,
                onDismiss = { showCustomDialog = false },
                onPlay = { chosen ->
                    val gameMap = chosen.toGameMap(idOverride = chosen.id)
                    val level = Level(
                        id = "custom_${chosen.id}",
                        name = "Custom: ${chosen.id}",
                        difficulty = chosen.difficulty,
                        games = listOf(gameMap)
                    )
                    onSelectGame(level, gameMap)
                    showCustomDialog = false
                }
            )
        }
    }
}