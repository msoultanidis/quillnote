package org.qosp.notes.ui.archive

import dagger.hilt.android.lifecycle.HiltViewModel
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
    override val provideNotes = noteRepository::getArchived
}
