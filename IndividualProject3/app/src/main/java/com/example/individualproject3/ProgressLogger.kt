package com.example.individualproject3

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ---------------------------------------------------------
   Parent & Child storage (JSON)
   Stored in internal storage as JSON objects / arrays.
--------------------------------------------------------- */

private const val PARENT_FILE = "parent_account.json"
private const val CHILDREN_FILE = "children.json"

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

/**
 * Handles writing gameplay attempts to a CSV log file.
 * Each line: timestamp, childName, levelId, gameId, resultCode, commandsCount
 */
class ProgressLogger(private val context: Context) {

    private val fileName = "progress_log.csv"
    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun logAttempt(
        childName: String?,
        levelId: String,
        gameId: String,
        resultCode: String,
        commandsCount: Int
    ) {
        val timestamp = dateFormat.format(Date())
        val safeChild = childName ?: ""

        val line =
            "$timestamp,$safeChild,$levelId,$gameId,$resultCode,$commandsCount\n"

        context.openFileOutput(fileName, Context.MODE_APPEND).use { fos ->
            fos.write(line.toByteArray())
        }
    }
}
/* ----------------- PARENT ACCOUNT ---------------------- */

/**
 * Loads the single parent account from JSON.
 */
fun loadParentAccount(context: Context): ParentAccount? {
    val file = context.getFileStreamPath(PARENT_FILE)
    if (!file.exists()) return null

    val text = file.readText().ifBlank { return null }
    val obj = JSONObject(text)

    return ParentAccount(
        id = obj.getString("id"),
        name = obj.getString("name"),
        pin = obj.getString("pin")
    )
}

/**
 * Saves parent account to JSON.
 */
fun saveParentAccount(context: Context, parent: ParentAccount) {
    val obj = JSONObject().apply {
        put("id", parent.id)
        put("name", parent.name)
        put("pin", parent.pin)
    }
    context.openFileOutput(PARENT_FILE, Context.MODE_PRIVATE).use { out ->
        out.write(obj.toString().toByteArray())
    }
}

/* ------------------ CHILD ACCOUNTS ---------------------- */

/**
 * Loads all child accounts from JSON.
 */
fun loadChildren(context: Context): List<ChildAccount> {
    val file = context.getFileStreamPath(CHILDREN_FILE)
    if (!file.exists()) return emptyList()

    val text = file.readText().ifBlank { return emptyList() }
    val arr = JSONArray(text)

    return List(arr.length()) { i ->
        val o = arr.getJSONObject(i)
        ChildAccount(
            id = o.getString("id"),
            name = o.getString("name"),
            age = o.optInt("age", -1).let { if (it == -1) null else it },
            notes = o.optString("notes", null),
            parentId = o.getString("parentId")
        )
    }
}

/**
 * Saves all children to JSON array.
 */
fun saveChildren(context: Context, children: List<ChildAccount>) {
    val arr = JSONArray()
    children.forEach { c ->
        val o = JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            c.age?.let { put("age", it) }
            c.notes?.let { put("notes", it) }
            put("parentId", c.parentId)
        }
        arr.put(o)
    }

    context.openFileOutput(CHILDREN_FILE, Context.MODE_PRIVATE).use { out ->
        out.write(arr.toString().toByteArray())
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