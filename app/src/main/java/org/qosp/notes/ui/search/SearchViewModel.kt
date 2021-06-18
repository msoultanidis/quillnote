package org.qosp.notes.ui.search

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.Notebook
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.ui.common.AbstractNotesViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    noteRepository: NoteRepository,
    notebookRepository: NotebookRepository,
    preferenceRepository: PreferenceRepository,
    syncManager: SyncManager,
) : AbstractNotesViewModel(preferenceRepository, syncManager) {
    private val searchKeyData: MutableStateFlow<String> = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    override val notesData: Flow<List<Note>> =
        notebookRepository.getAll().distinctUntilChanged().flatMapLatest { notebooks ->
            sortMethod.distinctUntilChanged().flatMapLatest { sortMethod ->
                searchKeyData.debounce(300).flatMapLatest { searchKey ->
                    noteRepository
                        .getAll(sortMethod)
                        .map { notes ->
                            getSearchResults(
                                searchKey.trim(),
                                notes,
                                notebooks
                            )
                        }
                }
            }
        }

    private fun getSearchResults(
        searchKey: String,
        notes: List<Note>,
        notebooks: List<Notebook>,
    ): List<Note> = notes.filter { note ->
        fun String.matches(): Boolean = contains(searchKey, true)

        when (note.isList) {
            true -> note.taskList.any { it.content.matches() }
            false -> note.content.matches()
        } ||
            note.title.matches() ||
            note.attachments.any { it.description.matches() } ||
            note.tags.any { it.name.matches() } ||
            notebooks.any { it.name.matches() && it.id == note.notebookId }
    }

    fun setSearchQuery(query: String) = viewModelScope.launch {
        searchKeyData.emit(query)
    }
}
