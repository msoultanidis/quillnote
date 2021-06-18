package org.qosp.notes.ui.archive

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.ui.common.AbstractNotesViewModel
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    noteRepository: NoteRepository,
    preferenceRepository: PreferenceRepository,
    syncManager: SyncManager
) : AbstractNotesViewModel(preferenceRepository, syncManager) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val notesData: Flow<List<Note>> = sortMethod.flatMapLatest {
        noteRepository.getArchived(it)
    }
}
