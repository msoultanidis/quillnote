package org.qosp.notes.ui.main

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.ui.common.AbstractNotesViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val notebookRepository: NotebookRepository,
    preferenceRepository: PreferenceRepository,
    syncManager: SyncManager,
) : AbstractNotesViewModel(preferenceRepository, syncManager) {

    private val notebookIdFlow: MutableStateFlow<Long?> = MutableStateFlow(null)

    override val notesData: Flow<List<Note>> = sortMethod.flatMapLatest { sortMethod ->
        notebookIdFlow.flatMapLatest { id ->
            if (id == null) noteRepository.getNonDeletedOrArchived(sortMethod) else getNotesByNotebookId(id)
        }
    }

    private fun getNotesByNotebookId(notebookId: Long) = sortMethod.flatMapLatest {
        noteRepository.getByNotebook(notebookId, it)
    }

    suspend fun notebookExists(notebookId: Long) = notebookRepository.getById(notebookId).firstOrNull() != null

    fun initialize(notebookId: Long?) {
        viewModelScope.launch { notebookIdFlow.emit(notebookId) }
    }
}
