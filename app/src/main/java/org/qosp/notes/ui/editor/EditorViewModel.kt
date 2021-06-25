package org.qosp.notes.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.msoul.datastore.defaultOf
import org.qosp.notes.data.model.*
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.*
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val notebookRepository: NotebookRepository,
    private val syncManager: SyncManager,
    private val preferenceRepository: PreferenceRepository,
) : ViewModel() {

    var inEditMode: Boolean = false
    var isNotInitialized = true

    private var syncJob: Job? = null
    private val noteIdFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data = noteIdFlow
        .filterNotNull()
        .flatMapLatest { noteRepository.getById(it) }
        .filterNotNull()
        .flatMapLatest { note ->
            getNotebookData(note.notebookId).flatMapLatest { notebook ->
                preferenceRepository.getAll().map { prefs ->
                    Data(
                        note = note,
                        notebook = notebook,
                        dateTimeFormats = prefs.dateFormat to prefs.timeFormat,
                        openMediaInternally = prefs.openMediaIn == OpenMediaIn.INTERNAL,
                        isInitialized = true,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Data(),
        )

    private fun getNotebookData(notebookId: Long?): Flow<Notebook?> {
        return notebookId?.let { id -> notebookRepository.getById(id) } ?: flow { emit(null) }
    }

    fun initialize(
        noteId: Long,
        newNoteTitle: String,
        newNoteContent: String,
        newNoteAttachments: List<Attachment>,
        newNoteIsList: Boolean,
        newNoteNotebookId: Long?,
    ) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                if (noteId > 0L) return@withContext noteId

                noteRepository.insertNote(
                    Note(
                        title = newNoteTitle,
                        content = newNoteContent,
                        notebookId = newNoteNotebookId,
                        isList = newNoteIsList,
                        attachments = newNoteAttachments,
                        isLocalOnly = preferenceRepository.get<NewNotesSyncable>().first() == NewNotesSyncable.NO
                    ),
                )
            }

            noteIdFlow.emit(id)

            isNotInitialized = false
        }
    }

    fun setNoteTitle(title: String) = update { note ->
        note.copy(
            title = title,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun setNoteContent(content: String) = update { note ->
        note.copy(
            content = content,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun setColor(color: NoteColor) = update { note ->
        note.copy(
            color = color,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun deleteAttachment(attachment: Attachment) = update { note ->
        note.copy(
            attachments = note.attachments
                .filterNot { it.path == attachment.path }
                .toMutableList(),
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun insertAttachments(vararg attachments: Attachment) = update { note ->
        note.copy(
            attachments = note.attachments + attachments,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun updateTaskList(list: List<NoteTask>) = update { note ->
        note.copy(
            taskList = list,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun toList() = update { note ->
        note.copy(
            content = "",
            isList = true,
            taskList = note.stringToTaskList(),
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun toTextNote() = update { note ->
        note.copy(
            content = note.taskListToString(),
            isList = false,
            taskList = listOf(),
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun disableMarkdown() = update { note ->
        note.copy(
            isMarkdownEnabled = false,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    fun enableMarkdown() = update { note ->
        note.copy(
            isMarkdownEnabled = true,
            modifiedDate = Instant.now().epochSecond,
        )
    }

    private inline fun update(crossinline transform: suspend (Note) -> Note) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = data.value.note ?: return@launch
            val new = transform(note)
            noteRepository.updateNotes(new, shouldSync = false)

            if (new.isLocalOnly) return@launch

            syncJob?.cancel()
            syncJob = launch {
                delay(300L) // To prevent multiple requests
                syncManager.updateOrCreate(new)
            }
        }
    }

    data class Data(
        val note: Note? = null,
        val notebook: Notebook? = null,
        val dateTimeFormats: Pair<DateFormat, TimeFormat> = defaultOf<DateFormat>() to defaultOf<TimeFormat>(),
        val openMediaInternally: Boolean = true,
        val isInitialized: Boolean = false,
    )
}
