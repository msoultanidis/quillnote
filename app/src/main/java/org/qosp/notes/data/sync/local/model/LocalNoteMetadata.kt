package org.qosp.notes.data.sync.local.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.qosp.notes.data.model.Attachment
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.NoteColor
import org.qosp.notes.data.model.NoteTask
import org.qosp.notes.data.model.Reminder
import org.qosp.notes.data.model.Tag
import org.qosp.notes.data.sync.core.SyncProvider
import java.time.Instant

@Serializable
data class LocalNoteMetadata(
    val id: Long = SyncProvider.NOT_SET_ID,
    val isList: Boolean = false,
    val taskList: List<NoteTask> = emptyList(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val isMarkdownEnabled: Boolean = true,
    val creationDate: Long = Instant.now().epochSecond,
    val deletionDate: Long? = null,
    val attachments: List<Attachment> = emptyList(),
    val color: NoteColor = NoteColor.Default,
    val tags: List<Tag> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
) {
    override fun toString(): String {
        return "$METADATA_START\n${Json.encodeToString(this)}\n$METADATA_END"
    }
}

const val METADATA_START = "%!% Quillnote Metadata Start %!%"
const val METADATA_END = "%!% Quillnote Metadata End %!%"

fun Note.extractMetadata(): LocalNoteMetadata {
    return LocalNoteMetadata(
        id = id,
        isList = isList,
        taskList = taskList,
        isArchived = isArchived,
        isDeleted = isDeleted,
        isPinned = isPinned,
        isHidden = isHidden,
        isMarkdownEnabled = isMarkdownEnabled,
        creationDate = creationDate,
        deletionDate = deletionDate,
        attachments = attachments,
        color = color,
        tags = tags,
        reminders = reminders,
    )
}