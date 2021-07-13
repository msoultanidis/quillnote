package org.qosp.notes.data.sync.local

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.qosp.notes.data.model.Attachment
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.NoteColor
import org.qosp.notes.data.model.NoteTask
import org.qosp.notes.data.model.Reminder
import org.qosp.notes.data.model.Tag
import org.qosp.notes.data.sync.core.InvalidConfig
import org.qosp.notes.data.sync.core.ProviderConfig
import org.qosp.notes.data.sync.core.RemoteNote
import org.qosp.notes.data.sync.core.Response
import org.qosp.notes.data.sync.core.SyncProvider
import org.qosp.notes.data.sync.local.model.LocalNote
import org.qosp.notes.data.sync.local.model.LocalNoteMetadata
import org.qosp.notes.preferences.CloudService
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets




fun lastModified(file: DocumentFile, context: Context): Long {
    var lastModified: Long = 0
    val cursor: Cursor? = context.contentResolver.query(file.uri, null, null, null, null)
    cursor?.use { cursor ->
        if (cursor.moveToFirst()) lastModified =
            cursor.getLong(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED))
    }
    return lastModified
}

fun InputStream.readAllText(): String {
    val stringBuilder = StringBuilder()
    BufferedReader(InputStreamReader(this,
        Charset.forName(StandardCharsets.UTF_8.name()))).use { reader ->
        var c = 0
        while (reader.read().also { c = it } != -1) {
            stringBuilder.append(c.toChar())
        }
    }

    return stringBuilder.toString()
}

