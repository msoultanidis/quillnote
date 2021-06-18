package org.qosp.notes.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.core.Success
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.*

abstract class AbstractNotesViewModel(
    protected val preferenceRepository: PreferenceRepository,
    protected val syncManager: SyncManager,
) : ViewModel() {
    abstract val notesData: Flow<List<Note>>
    val sortMethod = preferenceRepository.get<SortMethod>()
    val layoutMode = preferenceRepository.get<LayoutMode>()
    val noteDeletionTime = preferenceRepository.get<NoteDeletionTime>()

    suspend fun isSyncingEnabled(): Boolean = syncManager.ifSyncing { _, _ -> Success } == Success
}
