package org.qosp.notes.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            onDelete = ForeignKey.SET_NULL,
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebookId"]
        ),
    ]
)
@Serializable
data class NoteEntity(
    val title: String,
    val content: String,
    val isList: Boolean,
    val taskList: List<NoteTask>,
    val isArchived: Boolean,
    val isDeleted: Boolean,
    val isPinned: Boolean,
    val isHidden: Boolean,
    val isMarkdownEnabled: Boolean,
    val isLocalOnly: Boolean,
    val creationDate: Long,
    val modifiedDate: Long,
    val deletionDate: Long?,
    val attachments: List<Attachment>,
    val color: NoteColor,
    @ColumnInfo(index = true)
    val notebookId: Long?,
    @PrimaryKey(autoGenerate = true)
    val id: Long,
)

@Serializable
@Parcelize
data class Note(
    val title: String = "",
    val content: String = "",
    val isList: Boolean = false,
    val taskList: List<NoteTask> = listOf(),
    val isArchived: Boolean = false,
    val isDeleted: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val isMarkdownEnabled: Boolean = true,
    val isLocalOnly: Boolean = false,
    val creationDate: Long = Instant.now().epochSecond,
    val modifiedDate: Long = Instant.now().epochSecond,
    val deletionDate: Long? = null,
    val attachments: List<Attachment> = listOf(),
    val color: NoteColor = NoteColor.Default,
    val notebookId: Long? = null,
    val id: Long = 0L,
    @Relation(
        entity = Tag::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagJoin::class,
            parentColumn = "noteId",
            entityColumn = "tagId",
        )
    )
    val tags: List<Tag> = listOf(),
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId",
    )
    val reminders: List<Reminder> = listOf(),
) : Parcelable {

    fun isEmpty(): Boolean {
        val baseCondition = title.isBlank() && attachments.isEmpty() && reminders.isEmpty() && tags.isEmpty()
        return when {
            isList -> baseCondition && taskList.isEmpty()
            else -> baseCondition && content.isBlank()
        }
    }

    fun stringToTaskList(): List<NoteTask> {
        var nextId = 0L

        return content
            .lines()
            .map { NoteTask(nextId++, it.trim(), false) }
    }

    fun taskListToString(withCheckmarks: Boolean = false): String {
        return taskList.joinToString("\n") {
            val prefix = when {
                withCheckmarks -> if (it.isDone) "☑ " else "☐ "
                else -> ""
            }

            "$prefix${it.content.trim()}"
        }
    }

    fun toEntity(): NoteEntity = NoteEntity(
        title = title,
        content = content,
        isList = isList,
        taskList = taskList,
        isArchived = isArchived,
        isDeleted = isDeleted,
        isPinned = isPinned,
        isHidden = isHidden,
        isMarkdownEnabled = isMarkdownEnabled,
        isLocalOnly = isLocalOnly,
        creationDate = creationDate,
        modifiedDate = modifiedDate,
        deletionDate = deletionDate,
        attachments = attachments,
        color = color,
        notebookId = notebookId,
        id = id
    )
}
