package com.example.individualproject3

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressLogger(private val context: Context) {

    private val fileName = "progress_log.csv"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // NEW: this version logs childName too
    fun logAttempt(
        childName: String?,
        levelId: String,
        gameId: String,
        resultCode: String,
        commandsCount: Int
    ) {
        val timestamp = dateFormat.format(Date())
        val safeChild = childName ?: ""
        // timestamp,childName,levelId,gameId,resultCode,commandsCount
        val line = "$timestamp,$safeChild,$levelId,$gameId,$resultCode,$commandsCount\n"

        context.openFileOutput(fileName, Context.MODE_APPEND).use { fos ->
            fos.write(line.toByteArray())
        }
    }
}

// ---------- Parent / Child storage in JSON ----------

private const val PARENT_FILE = "parent_account.json"
private const val CHILDREN_FILE = "children.json"

// -------- Parent storage --------

fun loadParentAccount(context: Context): ParentAccount? {
    val file = context.getFileStreamPath(PARENT_FILE)
    if (!file.exists()) return null

    val text = file.readText()
    if (text.isBlank()) return null

    val obj = JSONObject(text)
    return ParentAccount(
        id = obj.getString("id"),
        name = obj.getString("name"),
        pin = obj.getString("pin")
    )
}

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

// -------- Children storage --------

fun loadChildren(context: Context): List<ChildAccount> {
    val file = context.getFileStreamPath(CHILDREN_FILE)
    if (!file.exists()) return emptyList()

    val text = file.readText()
    if (text.isBlank()) return emptyList()

    val arr = JSONArray(text)
    val result = mutableListOf<ChildAccount>()
    for (i in 0 until arr.length()) {
        val o = arr.getJSONObject(i)
        result += ChildAccount(
            id = o.getString("id"),
            name = o.getString("name"),
            age = if (o.has("age")) o.optInt("age") else null,
            notes = if (o.has("notes")) o.optString("notes") else null,
            parentId = o.getString("parentId")
        )
    }
    return result
}

fun saveChildren(context: Context, children: List<ChildAccount>) {
    val arr = JSONArray()
    for (c in children) {
        val o = JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            if (c.age != null) put("age", c.age)
            if (c.notes != null) put("notes", c.notes)
            put("parentId", c.parentId)
        }
        arr.put(o)
    }
    context.openFileOutput(CHILDREN_FILE, Context.MODE_PRIVATE).use { out ->
        out.write(arr.toString().toByteArray())
    }
}
