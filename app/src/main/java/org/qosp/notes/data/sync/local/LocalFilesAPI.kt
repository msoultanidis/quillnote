package org.qosp.notes.data.sync.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.sync.local.model.LocalNote
import org.qosp.notes.data.sync.local.model.LocalNoteMetadata
import org.qosp.notes.data.sync.local.model.METADATA_END
import org.qosp.notes.data.sync.local.model.METADATA_START
import java.io.OutputStreamWriter
import java.util.regex.Matcher
import java.util.regex.Pattern

class LocalFilesAPI(private val context: Context) {
    fun deleteNoteFile(note: LocalNote) {
        note.extras
            ?.let { DocumentFile.fromSingleUri(context, Uri.parse(it)) }
            ?.delete()
    }

    fun createNoteFile(note: LocalNote, config: LocalProviderConfig): DocumentFile {
        // Establish the directory. root or notebook
        // Check for note with same filename/title
        val notebookDir = when {
            note.notebookName.isBlank() -> config.dir
            else -> config.dir.listFiles().find { it.isDirectory && it.name == note.notebookName }
                ?: config.dir.createDirectory(note.notebookName) ?: throw Exception("Bad notebook name")
        }

        var filename: String = note.title
        while (notebookDir.findFile(filename) != null) {
            when {
                filename.matches(Regex("[^/]* \\([0-9]+\\)")) -> {
                    val number = Regex("[0-9]+").findAll(filename).last().value.toInt()
                    filename = filename.replaceLast("[0-9]+", "${number.inc()}")
                }
                else -> {
                    filename += " (1)"
                }
            }
        }

        val noteFile = notebookDir.createFile("text/markdown", filename) ?: throw Exception("Note was not created.")

        context.contentResolver.openOutputStream(noteFile.uri)?.use { out ->
            val serializedMetadata = note.metadata?.toString().orEmpty()
            val fileContents = "${note.content}$serializedMetadata"
            out.write(fileContents.toByteArray())
        }

        return noteFile
    }

    fun getNoteFromFile(file: DocumentFile, config: LocalProviderConfig): LocalNote? {
        val title = file.name?.substringBeforeLast(".") ?: return null

        val text = context.contentResolver.openInputStream(file.uri)?.use { it.readAllText() } ?: return null

        val content = text.substringBeforeLast(METADATA_START)

        val metadata = text
            .substringAfterLast(METADATA_START, "")
            .substringBeforeLast(METADATA_END, "")
            .runCatching { Json.decodeFromString<LocalNoteMetadata>(this) }
            .getOrNull()

        val notebookName = file.parentFile?.let { parent ->
            when (parent.uri) {
                config.dir.uri -> ""
                else -> parent.name
            }
        }.orEmpty()


        return LocalNote(
            content = content,
            title = title,
            metadata = metadata,
            notebookName = notebookName,
            dateModified = lastModified(file, context) / 1000,
            extras = file.uri.toString(),
        )
    }

    fun getAllNoteFiles(config: LocalProviderConfig): Sequence<DocumentFile> {
        return config.dir.listFiles()
            .asSequence()
            .map { file ->
                when {
                    file.isNoteFile -> listOfNotNull(file)
                    file.isDirectory -> file.listFiles().filter { it.isNoteFile }
                    else -> emptyList()
                }
            }
            .flatten()
    }

    fun getAll(config: LocalProviderConfig): List<LocalNote> {
        return getAllNoteFiles(config)
            .mapNotNull { getNoteFromFile(it, config) }
            .toList()
    }

    fun String.replaceLast(regex: String, replacement: String): String {
        val pattern: Pattern = Pattern.compile(regex)
        val matcher: Matcher = pattern.matcher(this)
        if (!matcher.find()) {
            return this
        }
        var lastMatchStart = 0
        do {
            lastMatchStart = matcher.start()
        } while (matcher.find())
        matcher.find(lastMatchStart)
        val sb = StringBuffer(length)
        matcher.appendReplacement(sb, replacement)
        matcher.appendTail(sb)
        return sb.toString()
    }

    fun appendMetadataToFile(note: LocalNote, metadata: LocalNoteMetadata) {
        val uri = note.extras?.let { Uri.parse(it) } ?: return

        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write("${note.content}$metadata".toByteArray())
        }
    }

    fun updateNote(note: LocalNote, mapping: IdMapping, config: LocalProviderConfig): LocalNote {
        val uri = note.extras?.let { Uri.parse(it) } ?: throw Exception("Could not parse uri")
        val oldFile = DocumentFile.fromSingleUri(context, uri) ?: throw Exception("Could not find file")

        if (note.title != oldFile.name) {
            oldFile.renameTo(note.title)
        }

        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write("${note.content}${note.metadata?.toString().orEmpty()}".toByteArray())
        }

        return note
    }

    val DocumentFile.isNoteFile get() = isFile && type?.startsWith("text") == true
}