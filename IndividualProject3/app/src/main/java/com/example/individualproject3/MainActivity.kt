@file:OptIn(ExperimentalFoundationApi::class)
package com.example.individualproject3

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.content.ClipData
import android.content.ClipDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter

/**notes:
 * got the fail sound filr here:
 * https://www.myinstants.com/en/instant/botw-game-over-25928/
 *
 * got the victory sound file here:
 * https://www.myinstants.com/en/instant/vlp-zelda-victory-94573/
 *
 * got floor tiles and wall tiles here:
 * https://www.spriters-resource.com/gamecube/legendofzeldafourswordsadventures/asset/93739/
 *
 * got falling in water soundeffect here:
 * https://pixabay.com/sound-effects/falling-game-character-352287/
 *
 * got the bump into wall sound effect here:
 * https://pixabay.com/sound-effects/thump-105302/
 *
 * took bits and pieces for the map design from this website:
 * https://www.spriters-resource.com/game_boy_advance/thelegendofzeldaalinktothepast/asset/20701/
 */
// TODO: Added small change so Git can detect modifications


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KodableApp()
                }
            }
        }
    }
}

// All the screens in your app
enum class Screen {
    PARENT_LOGIN,
    PARENT_HOME,
    MAIN_MENU,
    LEVEL_SELECT,
    GAME,
    LEVEL_EDITOR,
    PARENT_STATS
}


data class Account(
    val username: String,
    val password: String,
    val isParent: Boolean,
    val parentUsername: String? = null   // only used for kid accounts
)

// ---------- MAIN APP ROOT ----------

@Composable
fun KodableApp() {
    val context = LocalContext.current

    // -------------------------
    // STATE for accounts
    // -------------------------
    var currentScreen by remember { mutableStateOf(Screen.PARENT_LOGIN) }

    var parentAccount by remember {
        mutableStateOf(loadParentAccount(context))
    }

    var children by remember {
        mutableStateOf(loadChildren(context))
    }

    var currentChild by remember {
        mutableStateOf<ChildAccount?>(null)
    }



    // You can actually delete Account/currentKidName if you want,
    // but I'll keep them and just make currentKidName actually used.
    val accounts = remember { mutableStateListOf<Account>() }
    var currentKidName by remember { mutableStateOf<String?>(null) }

    // -------------------------
    // GAME STATE
    // -------------------------
    var selectedLevel by remember { mutableStateOf<Level?>(null) }
    var selectedGameMap by remember { mutableStateOf<GameMap?>(null) }

    // All levels (easy + hard)
    val allLevels = remember { createAllLevels() }

    // -------------------------
    // SCREEN ROUTING
    // -------------------------
    when (currentScreen) {

        Screen.PARENT_LOGIN -> {
            ParentLoginScreen(
                existingParent = parentAccount,
                onParentCreatedOrLoggedIn = { parent ->
                    parentAccount = parent
                    // reload children after reset
                    children = loadChildren(context)
                    currentChild = null
                    currentKidName = null
                    currentScreen = Screen.PARENT_HOME
                }
            )
        }

        Screen.PARENT_HOME -> {
            ParentHomeScreen(
                parent = parentAccount!!,
                children = children,
                currentChild = currentChild,
                onChildrenChanged = { updated ->
                    children = updated
                    saveChildren(context, updated)
                },
                onSelectChild = { child ->
                    currentChild = child
                    currentKidName = child.name      // 🔹 IMPORTANT: set kid name here
                },
                onPlayAsChild = {
                    if (currentChild != null) {
                        currentScreen = Screen.LEVEL_SELECT
                    }
                },
                onOpenEditor = {
                    currentScreen = Screen.LEVEL_EDITOR
                },
                onViewStats = {
                    currentScreen = Screen.PARENT_STATS
                },
                onLogout = {
                    currentChild = null
                    currentKidName = null
                    currentScreen = Screen.PARENT_LOGIN
                }
            )
        }

        Screen.MAIN_MENU -> {
            MainMenuScreen(
                currentChild = currentChild!!,
                onBackToParent = { currentScreen = Screen.PARENT_HOME },
                onSelectDifficulty = { difficulty ->
                    currentScreen = Screen.LEVEL_SELECT
                }
            )
        }

        Screen.LEVEL_SELECT -> {
            LevelSelectScreen(
                levels = allLevels,
                onBack = { currentScreen = Screen.PARENT_HOME },
                onSelectGame = { level, gameMap ->
                    selectedLevel = level
                    selectedGameMap = gameMap
                    currentScreen = Screen.GAME
                }
            )
        }


        Screen.GAME -> {
            GameScreen(
                level = selectedLevel!!,
                gameMap = selectedGameMap!!,
                currentKidName = currentKidName,
                onBack = { currentScreen = Screen.LEVEL_SELECT }
            )
        }

        // 6) LEVEL EDITOR SCREEN
        Screen.LEVEL_EDITOR -> {
            LevelEditorScreen(
                onBack = { currentScreen = Screen.PARENT_HOME }
            )
        }

        // 🔹 Parent stats screen now gets the children list
        Screen.PARENT_STATS -> {
            ParentStatsScreen(
                children = children,
                onBack = { currentScreen = Screen.PARENT_HOME }
            )
        }
    }
}






// ---------- MAIN MENU SCREEN (per-child) ----------
@Composable
fun MainMenuScreen(
    currentChild: ChildAccount,
    onBackToParent: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome, ${currentChild.name}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(24.dp))

            Text("Choose difficulty:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { onSelectDifficulty(Difficulty.EASY) }) {
                    Text("Easy")
                }
                Button(onClick = { onSelectDifficulty(Difficulty.HARD) }) {
                    Text("Hard")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(onClick = onBackToParent) {
                Text("Back")
            }
        }
    }
}




// ---------- PARENT LOGIN SCREEN ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentLoginScreen(
    existingParent: ParentAccount?,
    onParentCreatedOrLoggedIn: (ParentAccount) -> Unit
) {
    val context = LocalContext.current

    // if existing parent -> default login mode; else registration
    var registrationMode by remember { mutableStateOf(existingParent == null) }

    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    val hasExistingParent = existingParent != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Login") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if (registrationMode) {
                Text(
                    text = "Create Parent Account",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        pin = value.filter { it.isDigit() }.take(6)
                        status = ""
                    },
                    label = { Text("PIN (numbers)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isBlank() || pin.length < 3) {
                            status = "Enter a name and a PIN (at least 3 digits)."
                        } else {
                            val parent = ParentAccount(
                                id = "parent_1",
                                name = name.trim(),
                                pin = pin
                            )
                            saveParentAccount(context, parent)
                            status = ""
                            onParentCreatedOrLoggedIn(parent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Create Account")
                }

                if (hasExistingParent) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This will overwrite the existing parent and children.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                // LOGIN MODE
                Text(
                    text = "Welcome, ${existingParent!!.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        pin = value.filter { it.isDigit() }.take(6)
                        status = ""
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val p = existingParent
                        if (p != null && pin == p.pin) {
                            status = ""
                            onParentCreatedOrLoggedIn(p)
                        } else {
                            status = "Incorrect PIN."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        // delete old parent, children, and stats from internal storage
                        context.deleteFile("parent_account.json")
                        context.deleteFile("children.json")
                        context.deleteFile("progress_log.csv")   // 🔹 reset stats too, if you want

                        registrationMode = true
                        status = "Old parent removed. Create a new parent account."
                        name = ""
                        pin = ""
                    }
                ) {
                    Text("Register New Parent (Reset)")
                }

            }

            if (status.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(text = status, color = Color.Red)
            }
        }
    }
}


// ---------- PARENT HOME SCREEN ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    parent: ParentAccount,
    children: List<ChildAccount>,
    currentChild: ChildAccount?,
    onChildrenChanged: (List<ChildAccount>) -> Unit,
    onSelectChild: (ChildAccount) -> Unit,
    onPlayAsChild: () -> Unit,
    onOpenEditor: () -> Unit,
    onViewStats: () -> Unit,
    onLogout: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Home") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
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
            horizontalAlignment = Alignment.Start
        ) {
            Text("Logged in as: ${parent.name}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Text("Children:", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))

            if (children.isEmpty()) {
                Text("No children registered yet.")
            } else {
                children.forEach { child ->
                    val isActive = currentChild?.id == child.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectChild(child) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = child.name + if (isActive) " (Active)" else "",
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                val updated = children.filter { it.id != child.id }
                                onChildrenChanged(updated)
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register New Child")
            }

            Spacer(Modifier.height(16.dp))

            Text("Current active child: ${currentChild?.name ?: "None"}")

            Spacer(Modifier.height(8.dp))

            // 🔹 3) Rename this button "Play Game"
            Button(
                onClick = {
                    if (currentChild == null) {
                        status = "Select a child first."
                    } else {
                        status = ""
                        onPlayAsChild()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Play Game")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onOpenEditor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Level Editor")
            }

            Spacer(Modifier.height(8.dp))

            // 🔹 New: View Stats button
            Button(
                onClick = onViewStats,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Stats")
            }

            if (status.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(status, color = Color.Red)
            }
        }

        if (showAddDialog) {
            AddChildDialog(
                onDismiss = { showAddDialog = false },
                onChildCreated = { name, age, notes ->
                    val newChild = ChildAccount(
                        id = "child_${System.currentTimeMillis()}",
                        name = name,
                        age = age,
                        notes = notes,
                        parentId = parent.id
                    )
                    onChildrenChanged(children + newChild)
                    showAddDialog = false
                }
            )
        }
    }
}


// ---------- DIALOG TO ADD CHILD ----------

@Composable
fun AddChildDialog(
    onDismiss: () -> Unit,
    onChildCreated: (String, Int?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Child") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Child Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Age (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        error = "Name is required."
                    } else {
                        val age = ageStr.takeIf { it.isNotBlank() }?.toIntOrNull()
                        onChildCreated(name.trim(), age, notes.takeIf { it.isNotBlank() })
                    }
                }
            ) {
                Text("Save")
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

    // All saved custom levels
    var customLevels by remember { mutableStateOf<List<SavedCustomLevel>>(emptyList()) }
    var showCustomDialog by remember { mutableStateOf(false) }

    // Load custom levels when we enter this screen
    LaunchedEffect(Unit) {
        customLevels = loadCustomLevels(context)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Game") },
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
                .padding(16.dp)
                .verticalScroll(scrollState)   // 🔹 now scrollable
        ) {
            // ---- Built-in levels & games ----
            levels.forEach { level ->
                Text(
                    "${level.name} (${level.difficulty})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                level.games.forEach { game ->
                    Button(
                        onClick = { onSelectGame(level, game) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Play game: ${game.id}")
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // ---- Custom levels section (single button) ----
            Text("Custom Levels", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            if (customLevels.isEmpty()) {
                Text(
                    "No custom levels saved yet.\nUse the Level Editor to create one.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Button(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play Custom Level")
                }
            }
        }

        // Dialog to choose which custom level to play
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
fun LoginScreen(
    isParent: Boolean,
    accounts: MutableList<Account>,
    onBack: () -> Unit,
    onLoggedIn: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    // For kid registration: which parent owns this kid?
    var selectedParent by remember { mutableStateOf<String?>(null) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }


    val title = if (isParent) "Parent Login" else "Kid Login"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRegisterMode) "Register New ${if (isParent) "Parent" else "Kid"}" else "Log In",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    errorMessage = ""
                },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // If we're registering a KID, show parent selection
            if (!isParent && isRegisterMode) {
                Text("Select Parent Account:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))

                Box {
                    Button(onClick = { parentDropdownExpanded = true }) {
                        Text(selectedParent ?: "Choose Parent")
                    }

                    DropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        val parentAccounts = accounts.filter { it.isParent }
                        if (parentAccounts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No parent accounts yet") },
                                onClick = { parentDropdownExpanded = false }
                            )
                        } else {
                            parentAccounts.forEach { parentAcc ->
                                DropdownMenuItem(
                                    text = { Text(parentAcc.username) },
                                    onClick = {
                                        selectedParent = parentAcc.username
                                        parentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both username and password."
                        return@Button
                    }

                    if (isRegisterMode) {
                        if (isParent) {
                            // PARENT REGISTRATION
                            accounts.add(
                                Account(
                                    username = username,
                                    password = password,
                                    isParent = true
                                )
                            )
                            onLoggedIn(username)
                        } else {
                            // KID REGISTRATION → MUST CHOOSE A PARENT
                            if (selectedParent == null) {
                                errorMessage = "Please choose a parent account."
                                return@Button
                            }

                            accounts.add(
                                Account(
                                    username = username,
                                    password = password,
                                    isParent = false,
                                    parentUsername = selectedParent!!
                                )
                            )
                            onLoggedIn(username)
                        }
                    } else {
                        // LOGIN
                        val account = accounts.find {
                            it.username == username && it.isParent == isParent
                        }
                        if (account == null) {
                            errorMessage = "Account not found. Try registering."
                        } else if (account.password != password) {
                            errorMessage = "Incorrect password."
                        } else {
                            onLoggedIn(username)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRegisterMode) "Register" else "Log In")
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    errorMessage = ""
                }
            ) {
                Text(
                    if (isRegisterMode)
                        "Already have an account? Log in"
                    else
                        "New here? Register"
                )
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

fun commandLabel(cmd: Command): String =
    when (cmd) {
        Command.MOVE_UP -> "↑"
        Command.MOVE_DOWN -> "↓"
        Command.MOVE_LEFT -> "←"
        Command.MOVE_RIGHT -> "→"
    }

fun stepPosition(
    currentPos: Pair<Int, Int>,
    cmd: Command
): Pair<Int, Int> {
    val (x, y) = currentPos
    return when (cmd) {
        Command.MOVE_UP -> x to (y - 1)
        Command.MOVE_DOWN -> x to (y + 1)
        Command.MOVE_LEFT -> (x - 1) to y
        Command.MOVE_RIGHT -> (x + 1) to y
    }
}

@Composable
fun chooseWallSprite(
    x: Int,
    y: Int,
    map: GameMap,
    horizontal: Painter,
    vertical: Painter,
    corner: Painter
): Painter {

    val maxX = map.width - 1
    val maxY = map.height - 1

    return when {
        // Corners
        x == 0 && y == 0 -> corner           // top-left
        x == maxX && y == 0 -> corner        // top-right
        x == 0 && y == maxY -> corner        // bottom-left
        x == maxX && y == maxY -> corner     // bottom-right

        // Top or bottom row → horizontal wall
        y == 0 || y == maxY -> horizontal

        // Left or right column → vertical wall
        x == 0 || x == maxX -> vertical

        // Otherwise fall back to your map-defined interior walls
        else -> horizontal
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    kidNames: List<String>,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // All log entries from file
    var allEntries by remember { mutableStateOf<List<ProgressEntry>>(emptyList()) }

    // Which child is selected (null = all children of this parent)
    var selectedKid by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    // Load stats once when screen opens
    LaunchedEffect(Unit) {
        allEntries = readProgressEntries(context)
    }

    // Only consider entries for this parent's children
    val kidSet = kidNames.toSet()
    val entriesForThisParent = allEntries.filter { entry ->
        entry.childName.isNotBlank() && entry.childName in kidSet
    }

    // Then filter by selected child (if any)
    val filteredEntries = if (selectedKid == null) {
        entriesForThisParent
    } else {
        entriesForThisParent.filter { it.childName == selectedKid }
    }

    val totalAttempts = filteredEntries.size

    // Group by result code
    val counts = filteredEntries.groupingBy { it.resultCode }.eachCount()
    val codesInOrder = listOf("SUCCESS", "HIT_WALL", "OUT_OF_BOUNDS", "NO_GOAL", "UNKNOWN")

    val resultStats: List<ResultStat> = codesInOrder.map { code ->
        val count = counts[code] ?: 0
        ResultStat(
            code = code,
            label = when (code) {
                "SUCCESS" -> "Success"
                "HIT_WALL" -> "Hit Wall"
                "OUT_OF_BOUNDS" -> "Out of Bounds"
                "NO_GOAL" -> "Finished w/o Goal"
                else -> "Other / Unknown"
            },
            count = count
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Dashboard") },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Child Progress Summary",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(8.dp))

            // 🔹 Child selector
            if (kidNames.isNotEmpty()) {
                Text("Filter by child:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))

                var dropdownExpanded by remember { mutableStateOf(false) }

                Box {
                    Button(onClick = { dropdownExpanded = true }) {
                        Text(selectedKid ?: "All Children")
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Children") },
                            onClick = {
                                selectedKid = null
                                dropdownExpanded = false
                            }
                        )
                        kidNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedKid = name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            } else {
                Text("No kid accounts found for this parent.")
                Spacer(Modifier.height(16.dp))
            }

            if (totalAttempts == 0) {
                Text("No attempts logged yet for this selection.")
            } else {
                Text("Total Attempts: $totalAttempts")
                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Attempts by Outcome:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                val maxCount = resultStats.maxOfOrNull { it.count } ?: 0

                resultStats.forEach { stat ->
                    if (stat.count > 0) {
                        ResultBarRow(
                            label = stat.label,
                            count = stat.count,
                            maxCount = maxCount
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Note: These stats are based on runs performed while logged in as each child.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun ResultBarRow(
    label: String,
    count: Int,
    maxCount: Int
) {
    val fraction = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f

    Column {
        Text("$label: $count")
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
            ) {
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(Color(0xFF4CAF50), RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

data class ProgressEntry(
    val childName: String,
    val levelId: String,
    val gameId: String,
    val resultCode: String,
    val commandsCount: Int
)

data class ResultStat(
    val code: String,
    val label: String,
    val count: Int
)

fun readProgressEntries(context: Context): List<ProgressEntry> {
    val fileName = "progress_log.csv"

    return try {
        val text = context.openFileInput(fileName).bufferedReader().use { it.readText() }

        text.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.split(',')

                when {
                    // NEW format: timestamp, childName, levelId, gameId, resultCode, commandsCount
                    parts.size >= 6 -> {
                        val childName = parts[1]
                        val levelId = parts[2]
                        val gameId = parts[3]
                        val resultCode = parts[4]
                        val commandsCount = parts[5].toIntOrNull() ?: 0
                        ProgressEntry(childName, levelId, gameId, resultCode, commandsCount)
                    }
                    // OLD format (before child name): timestamp, levelId, gameId, resultCode, commandsCount
                    parts.size >= 5 -> {
                        val childName = ""  // unknown
                        val levelId = parts[1]
                        val gameId = parts[2]
                        val resultCode = parts[3]
                        val commandsCount = parts[4].toIntOrNull() ?: 0
                        ProgressEntry(childName, levelId, gameId, resultCode, commandsCount)
                    }
                    else -> null
                }
            }
            .toList()
    } catch (_: Exception) {
        emptyList()
    }
}


// ---------- Demo data so the app actually runs ----------

fun createAllLevels(): List<Level> {

    // -------- EASY 1 (your baked custom level) --------
    val easy1Tiles = listOf(
        listOf("empty", "empty", "empty", "empty", "empty", "tl_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty")
    )

    // added change here
    val easyGame1 = gameMapFromTileIds(
        id = "easy1_default",
        startX = 1,
        startY = 6,
        goalX = 6,
        goalY = 1,
        tileIds = easy1Tiles
    )

    val easy2Tiles = listOf(
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "tr_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "top_upper", "top_upper", "top_upper", "tl_upper", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "right_lower", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "bottom_lower", "inner_tr", "floor", "right_lower", "empty", "empty", "empty", "empty"),
        listOf("empty", "empty", "empty", "left_lower", "floor", "inner_bl", "top_lower", "top_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "left_lower", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "inner_tr", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "empty", "bl_lower", "bottom_lower", "br_lower")
    )

    val easyGame2 = gameMapFromTileIds(
        id = "easy level 2",
        startX = 8,
        startY = 8,
        goalX = 8,
        goalY = 1,
        tileIds = easy2Tiles
    )


    val easy3Tiles = listOf(
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "empty", "top_lower", "top_lower", "tr_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "right_lower", "empty", "left_lower", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_wall", "inner_wall", "floor", "inner_bl", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "top_upper", "top_upper", "top_upper", "floor", "top_upper", "top_upper", "top_upper", "top_upper", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "inner_tr", "floor", "inner_wall", "inner_wall", "inner_wall", "floor", "right_lower"),
        listOf("left_lower", "floor", "right_lower", "left_lower", "floor", "inner_wall", "inner_wall", "inner_wall", "floor", "right_lower"),
        listOf("left_lower", "floor", "right_lower", "left_lower", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("bl_lower", "bottom_lower", "br_lower", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower")
    )

    val easyGame3 = gameMapFromTileIds(
        id = "easy level 3",
        startX = 1,
        startY = 8,
        goalX = 8,
        goalY = 1,
        tileIds = easy3Tiles
    )

    val hard1Tiles = listOf(
        listOf("water", "water", "water", "water", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("bl_lower", "bottom_lower", "left_lower", "floor", "floor", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("water", "water", "left_lower", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("water", "tl_lower", "inner_br", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("tl_lower", "inner_br", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water")
    )

    val hardGame1 = gameMapFromTileIds(
        id = "Hard level 1",
        startX = 2,
        startY = 10,
        goalX = 10,
        goalY = 4,
        tileIds = hard1Tiles
    )

    val hard2Tiles = listOf(
        listOf("water", "top_upper", "floor", "floor", "floor", "floor", "top_upper", "floor", "floor", "floor", "top_upper", "water"),
        listOf("water", "floor", "floor", "top_upper", "floor", "floor", "floor", "water", "top_upper", "floor", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "water", "water", "water", "water", "top_upper", "water", "floor", "water"),
        listOf("top_upper", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "top_upper"),
        listOf("water", "water", "water", "floor", "water", "inner_wall", "water", "inner_wall", "floor", "water", "water", "water"),
        listOf("water", "water", "water", "floor", "water", "inner_wall", "water", "inner_wall", "floor", "water", "water", "water"),
        listOf("water", "water", "water", "floor", "floor", "floor", "floor", "floor", "floor", "top_upper", "water", "water"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water")
    )

    val hardGame2 = gameMapFromTileIds(
        id = "Hard level 2",
        startX = 3,
        startY = 10,
        goalX = 5,
        goalY = 5,
        tileIds = hard2Tiles
    )

    val hard3Tiles = listOf(
        listOf("tl_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "tr_upper"),
        listOf("left_upper", "tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "tr_lower", "right_upper"),
        listOf("left_upper", "left_lower", "water", "inner_wall", "water", "water", "water", "water", "water", "water", "water", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "inner_wall", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "inner_wall", "floor", "inner_wall", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "floor", "floor", "inner_wall", "floor", "floor", "floor", "inner_wall", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "floor", "floor", "floor", "floor", "floor", "inner_wall", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "inner_wall", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "water", "water", "water", "water", "water", "water", "water", "inner_wall", "water", "right_lower", "right_upper"),
        listOf("left_upper", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower", "right_upper"),
        listOf("bl_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "br_upper")
    )

    val hardGame3 = gameMapFromTileIds(
        id = "hard level 3",
        startX = 6,
        startY = 7,
        goalX = 7,
        goalY = 4,
        tileIds = hard3Tiles
    )

    // -------- EASY LEVEL COLLECTION --------
    val easyLevel = Level(
        id = "easy_level",
        name = "Easy Dungeons",
        difficulty = Difficulty.EASY,
        games = listOf(easyGame1,easyGame2,easyGame3)
    )

    // -------- HARD LEVEL (placeholder for now) --------
    val hardLevel = Level(
        id = "hard_level",
        name = "Hard Dungeons",
        difficulty = Difficulty.HARD,
        games = listOf(hardGame1, hardGame2,hardGame3)   // later you can add baked hardGame1, hardGame2, etc.
    )

    return listOf(easyLevel, hardLevel)
}



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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Command.values().forEach { cmd ->
                    val label = commandLabel(cmd)

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, Color.Black)
                            .background(Color.White)
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
                        Text(
                            text = label,
                            style = MaterialTheme.typography.headlineSmall
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    program.forEachIndexed { index, cmd ->
                        Surface {
                            Text(
                                text = "${index + 1}: ${commandLabel(cmd)}",
                                modifier = Modifier.padding(6.dp)
                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentStatsScreen(
    children: List<ChildAccount>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var allEntries by remember { mutableStateOf<List<ProgressEntry>>(emptyList()) }
    var selectedChildName by remember { mutableStateOf<String?>(null) }

    // Load all entries once
    LaunchedEffect(Unit) {
        allEntries = readProgressEntries(context)
    }

    val childNames = children.map { it.name }.distinct()

    // Filter entries for this parent’s children only
    val entriesForTheseKids = allEntries.filter { e ->
        e.childName.isNotBlank() && e.childName in childNames
    }

    // Then filter by selected child (or show all if null)
    val filteredEntries = if (selectedChildName == null) {
        entriesForTheseKids
    } else {
        entriesForTheseKids.filter { it.childName == selectedChildName }
    }

    val totalAttempts = filteredEntries.size

    val counts = filteredEntries.groupingBy { it.resultCode }.eachCount()
    val codesInOrder = listOf("SUCCESS", "HIT_WALL", "OUT_OF_BOUNDS", "NO_GOAL", "UNKNOWN")

    val resultStats: List<ResultStat> = codesInOrder.map { code ->
        val count = counts[code] ?: 0
        ResultStat(
            code = code,
            label = when (code) {
                "SUCCESS" -> "Success"
                "HIT_WALL" -> "Hit Wall"
                "OUT_OF_BOUNDS" -> "Out of Bounds"
                "NO_GOAL" -> "Finished w/o Goal"
                else -> "Other / Unknown"
            },
            count = count
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Stats") },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Child Progress Summary", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))

            // Child selector
            if (childNames.isNotEmpty()) {
                Text("Filter by child:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))

                var dropdownExpanded by remember { mutableStateOf(false) }

                Box {
                    Button(onClick = { dropdownExpanded = true }) {
                        Text(selectedChildName ?: "All Children")
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Children") },
                            onClick = {
                                selectedChildName = null
                                dropdownExpanded = false
                            }
                        )
                        childNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedChildName = name
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            } else {
                Text("No children registered.")
                Spacer(Modifier.height(16.dp))
            }

            if (totalAttempts == 0) {
                Text("No attempts logged yet for this selection.")
            } else {
                Text("Total Attempts: $totalAttempts")
                Spacer(Modifier.height(16.dp))

                Text("Attempts by Outcome:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                val maxCount = resultStats.maxOfOrNull { it.count } ?: 0

                resultStats.forEach { stat ->
                    if (stat.count > 0) {
                        ResultBarRow(
                            label = stat.label,
                            count = stat.count,
                            maxCount = maxCount
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Note: Stats are based on plays while logged in as each child.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper classes for stats
data class LevelStat(
    val levelId: String,
    val gameId: String,
    val total: Int,
    val success: Int
)

private data class LevelStatMutable(
    val levelId: String,
    val gameId: String,
    var total: Int = 0,
    var success: Int = 0
) {
    fun toImmutable() = LevelStat(levelId, gameId, total, success)
}

