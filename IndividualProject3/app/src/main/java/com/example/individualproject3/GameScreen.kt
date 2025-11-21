package com.example.individualproject3

import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//--------The game screen-------------//
//This is where the game screen is formed
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    level: Level,
    gameMap: GameMap,
    currentKidName: String?,   // 🔹 must be here
    onBack: () -> Unit
) {
    // Hero starts at the map start location
    var heroPos by remember {
        mutableStateOf(gameMap.startX to gameMap.startY)
    }

    // If this map came from the editor, it has a tile layout.
    // In that case we should NOT use the auto outer-wall rings.
    val hasTileLayout = gameMap.tileIds != null

    // NEW: track facing direction for sprite orientation
    var heroFacing by remember {
        mutableStateOf(HeroFacing.DOWN)
    }

    // Small offset for bonk shake (x,y in dp)
    var heroShake by remember {
        mutableStateOf(0 to 0)
    }

    // Sinking animation amount (0f to 1f)
    var heroSinkProgress by remember { mutableStateOf(0f) }

    // Commands the child drags into the program
    val program = remember { mutableStateListOf<Command>() }

    // Disable run button while program executes
    var isRunning by remember { mutableStateOf(false) }

    // Message shown under the grid (hit wall, success, etc.)
    var statusMessage by remember { mutableStateOf("") }

    // Result dialog state
    var showResultDialog by remember { mutableStateOf(false) }
    var resultTitle by remember { mutableStateOf("") }
    var resultBody by remember { mutableStateOf("") }
    var isSuccessResult by remember { mutableStateOf(false) }
    var runResultCode by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Logging + sound
    val context = LocalContext.current
    val logger = remember { ProgressLogger(context) }
    val soundManager = remember { SoundManager(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game: ${gameMap.id}") },
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
                .background(Color(0xFF071017)) // dungeon dark background
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Level: ${level.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            DungeonGrid(
                gameMap = gameMap,
                heroPos = heroPos,
                heroFacing = heroFacing,
                heroShake = heroShake,
                heroSinkProgress = heroSinkProgress
            )

            Spacer(Modifier.height(24.dp))

            // -------------------------------
            // COMMAND PALETTE
            // -------------------------------
            Text("Commands", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Drag arrows below into the drop area.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))

            // one base painter for all arrows
            val commandArrowPainter = painterResource(R.drawable.command_arrow)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Command.values().forEach { cmd ->

                    val rotation = when (cmd) {
                        Command.MOVE_UP    ->  90f
                        Command.MOVE_DOWN  -> -90f
                        Command.MOVE_LEFT  ->   0f
                        Command.MOVE_RIGHT -> 180f
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF1A242E)) // dark panel
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary, // your green accent
                                shape = RoundedCornerShape(10.dp)
                            )
                            .dragAndDropSource(
                                transferData = {
                                    DragAndDropTransferData(
                                        ClipData.newPlainText(
                                            "command",
                                            when (cmd) {
                                                Command.MOVE_UP    -> "UP"
                                                Command.MOVE_DOWN  -> "DOWN"
                                                Command.MOVE_LEFT  -> "LEFT"
                                                Command.MOVE_RIGHT -> "RIGHT"
                                            }
                                        )
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = commandArrowPainter,
                            contentDescription = cmd.name,
                            modifier = Modifier
                                .size(40.dp)
                                .graphicsLayer(rotationZ = rotation),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Program:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Drag-and-drop TARGET area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .border(2.dp, Color.DarkGray)
                    .dragAndDropTarget(
                        shouldStartDragAndDrop = { event ->
                            event.mimeTypes()
                                .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        },
                        target = remember {
                            object : DragAndDropTarget {
                                override fun onDrop(event: androidx.compose.ui.draganddrop.DragAndDropEvent): Boolean {
                                    val clipData = event.toAndroidDragEvent().clipData ?: return false
                                    if (clipData.itemCount < 1) return false
                                    val text = clipData.getItemAt(0).text?.toString() ?: return false

                                    val cmd = when (text) {
                                        "UP" -> Command.MOVE_UP
                                        "DOWN" -> Command.MOVE_DOWN
                                        "LEFT" -> Command.MOVE_LEFT
                                        "RIGHT" -> Command.MOVE_RIGHT
                                        else -> null
                                    }

                                    if (cmd != null && !isRunning) {
                                        program.add(cmd)
                                        return true
                                    }
                                    return false
                                }
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (program.isEmpty())
                        "Drag commands here"
                    else
                        "Drop more commands to extend program",
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            if (program.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    program.forEachIndexed { index, cmd ->
                        val rotation = when (cmd) {
                            Command.MOVE_UP    ->  90f
                            Command.MOVE_DOWN  -> -90f
                            Command.MOVE_LEFT  ->   0f
                            Command.MOVE_RIGHT -> 180f
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF222222)
                        ) {
                            Column(
                                modifier = Modifier.padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = commandArrowPainter,
                                    contentDescription = cmd.name,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .graphicsLayer(rotationZ = rotation),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (statusMessage.isNotEmpty()) {
                Text(statusMessage)
                Spacer(Modifier.height(8.dp))
            }

            // -----------------------
            // RUN / CLEAR / RESET
            //-----------------------
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // RUN BUTTON
                Button(
                    enabled = !isRunning && program.isNotEmpty(),
                    onClick = {
                        statusMessage = ""
                        showResultDialog = false
                        isSuccessResult = false
                        runResultCode = ""


                        scope.launch {
                            isRunning = true
                            heroPos = gameMap.startX to gameMap.startY
                            heroFacing = HeroFacing.DOWN
                            heroSinkProgress = 0f

                            var touchedWater = false
                            var reachedGoal = false

                            for (cmd in program) {

                                // 1) Update facing based on command
                                heroFacing = when (cmd) {
                                    Command.MOVE_UP    -> HeroFacing.UP
                                    Command.MOVE_DOWN  -> HeroFacing.DOWN
                                    Command.MOVE_LEFT  -> HeroFacing.LEFT
                                    Command.MOVE_RIGHT -> HeroFacing.RIGHT
                                }

                                // Direction vector for this command
                                val (dx, dy) = when (cmd) {
                                    Command.MOVE_UP    -> 0 to -1
                                    Command.MOVE_DOWN  -> 0 to 1
                                    Command.MOVE_LEFT  -> -1 to 0
                                    Command.MOVE_RIGHT -> 1 to 0
                                }

                                var movedThisCommand = false

                                // Slide in this direction until blocked
                                slideLoop@ while (true) {
                                    val nextX = heroPos.first + dx
                                    val nextY = heroPos.second + dy

                                    // --- OUT OF BOUNDS CHECK ---
                                    if (nextX !in 0 until gameMap.width || nextY !in 0 until gameMap.height) {
                                        if (!movedThisCommand) {
                                            // Immediately went off map -> fail
                                            statusMessage = "Went out of bounds!"
                                            resultTitle = "Out of Bounds"
                                            resultBody = "Your program moved Link off the map."
                                            isSuccessResult = false
                                            runResultCode = "OUT_OF_BOUNDS"
                                            soundManager.playFailure()
                                            showResultDialog = true
                                            isRunning = false
                                            break
                                        } else {
                                            // We were sliding and hit edge; just stop on last valid tile
                                            break@slideLoop
                                        }
                                    }

                                    val isOuter = if (hasTileLayout) {
                                        false            // editor maps rely purely on placed wall tiles
                                    } else {
                                        isOuterWall(nextX, nextY, gameMap)
                                    }

                                    val isInner = gameMap.walls.contains(nextX to nextY)
                                    val isWater = gameMap.waterTiles.contains(nextX to nextY)

                                    // --- WATER: fall in & lose ---
                                    if (isWater) {
                                        heroPos = nextX to nextY
                                        soundManager.playSplash()

                                        // sink animation
                                        for (i in 0..10) {
                                            heroSinkProgress = i / 10f
                                            delay(50L)
                                        }

                                        statusMessage = "Link fell into the water!"
                                        resultTitle = "Splash!"
                                        resultBody = "Link fell into the water. Try a different program."
                                        isSuccessResult = false
                                        runResultCode = "WATER"
                                        showResultDialog = true
                                        isRunning = false
                                        break
                                    }

                                    // --- WALL: stop sliding (and possibly fail) ---
                                    if (isOuter || isInner) {
                                        if (!movedThisCommand) {
                                            // Already against wall when this command started → bonk AND fail
                                            soundManager.playBonk()
                                            heroShake = when (cmd) {
                                                Command.MOVE_UP    -> 0 to -4
                                                Command.MOVE_DOWN  -> 0 to 4
                                                Command.MOVE_LEFT  -> -4 to 0
                                                Command.MOVE_RIGHT -> 4 to 0
                                            }
                                            delay(80L)
                                            heroShake = 0 to 0

                                            statusMessage = "Link bumped into a wall!"
                                            resultTitle = "Bumped the Wall"
                                            resultBody = "Your program tried to move Link into a wall. Try a different set of commands."
                                            isSuccessResult = false
                                            runResultCode = "HIT_WALL"
                                            showResultDialog = true
                                            isRunning = false

                                            // stop this slide loop; the outer loop will see showResultDialog and break too
                                            break
                                        } else {
                                            // We slid at least one tile, then hit a wall → just stop sliding
                                            break
                                        }
                                    }


                                    // --- SAFE TILE: move one step and keep sliding ---
                                    heroPos = nextX to nextY
                                    movedThisCommand = true

                                    // Check goal after each step of the slide
                                    if (heroPos.first == gameMap.goalX && heroPos.second == gameMap.goalY) {
                                        statusMessage = "Reached the goal!"
                                        resultTitle = "Great Job!"
                                        resultBody = "You guided Link successfully."
                                        isSuccessResult = true
                                        runResultCode = "SUCCESS"
                                        soundManager.playSuccess()
                                        showResultDialog = true
                                        isRunning = false
                                        break
                                    }

                                    // Show intermediate motion
                                    delay(200L)
                                }

                                // After finishing this command’s slide, if a dialog is showing we already ended
                                if (showResultDialog) break
                            }

                            // After the for-loop over commands:
                            if (!showResultDialog) {
                                // All commands ran, no water, no out-of-bounds, no goal
                                statusMessage = "Program finished, but Link did not reach the goal."
                                resultTitle = "Try Again"
                                resultBody = "All commands ran, but Link never reached the goal."
                                isSuccessResult = false
                                runResultCode = "NO_GOAL"
                                soundManager.playFailure()
                                showResultDialog = true
                            }

                            // 8) If we didn’t already stop because of goal / water / out-of-bounds:
                            if (!showResultDialog) {
                                when {
                                    reachedGoal -> {
                                        // (Safety case; we already handle goal above)
                                        statusMessage = "Reached the goal!"
                                        resultTitle = "Great Job!"
                                        resultBody = "You guided Link successfully."
                                        isSuccessResult = true
                                        runResultCode = "SUCCESS"
                                        soundManager.playSuccess()
                                    }
                                    !touchedWater -> {
                                        // All commands finished, no water, no goal -> end-of-run failure
                                        statusMessage = "Program finished, but Link did not reach the goal."
                                        resultTitle = "Try Again"
                                        resultBody = "All commands ran, but Link never reached the goal."
                                        isSuccessResult = false
                                        runResultCode = "NO_GOAL"
                                        soundManager.playFailure()
                                    }
                                }
                                showResultDialog = true
                            }

                            logger.logAttempt(
                                childName = currentKidName,
                                levelId = level.id,
                                gameId = gameMap.id,
                                resultCode = if (runResultCode.isNotEmpty()) runResultCode else "UNKNOWN",
                                commandsCount = program.size
                            )



                            isRunning = false
                        }
                    }
                ) {
                    Text("Run")
                }

                // CLEAR PROGRAM
                Button(
                    enabled = !isRunning,
                    onClick = {
                        program.clear()
                        statusMessage = ""
                        showResultDialog = false
                    }
                ) {
                    Text("Clear")
                }

                // RESET HERO
                Button(
                    enabled = !isRunning,
                    onClick = {
                        heroPos = gameMap.startX to gameMap.startY
                        heroFacing = HeroFacing.DOWN
                        heroShake = 0 to 0              // NEW
                        statusMessage = ""
                        heroSinkProgress = 0f
                        showResultDialog = false
                        isSuccessResult = false
                        runResultCode = ""
                    }
                ) {
                    Text("Reset")
                }
            }

            Spacer(Modifier.height(16.dp))

            // RESULT DIALOG (success or fail)
            if (showResultDialog) {
                AlertDialog(
                    onDismissRequest = { /* forced */ },
                    title = { Text(resultTitle) },
                    text = { Text(resultBody) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                heroPos = gameMap.startX to gameMap.startY
                                heroFacing = HeroFacing.DOWN
                                heroSinkProgress = 0f
                                statusMessage = ""
                                showResultDialog = false
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                )
            }
        }
    }
}