package com.example.individualproject3

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

/* ---------------------------------------------------------
   Parent & Child storage (JSON)
   Stored in internal storage as JSON objects / arrays.
--------------------------------------------------------- */

private const val PARENT_FILE = "parent_account.json"
private const val CHILDREN_FILE = "children.json"

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
