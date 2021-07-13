package org.qosp.notes.data.sync.local.model

import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.core.RemoteNote
import org.qosp.notes.data.sync.core.SyncProvider
import org.qosp.notes.preferences.CloudService
import java.time.Instant

data class LocalNote(
    val title: String = "",
    val content: String = "",
    val notebookName: String = "",
    val metadata: LocalNoteMetadata? = null,
    val dateModified: Long = Instant.now().epochSecond,
    override val extras: String? = "",
) : RemoteNote {

    override val provider = CloudService.LOCAL
    override val id get() = metadata?.id ?: SyncProvider.NOT_SET_ID

    // TODO: Add equality checks for content as well
    override fun compareTo(localNote: Note, mapping: IdMapping): RemoteNote.Comparison {
        return when {
            dateModified == localNote.modifiedDate -> RemoteNote.IsSame
            dateModified > localNote.modifiedDate -> RemoteNote.IsNewer
            else -> RemoteNote.IsOutdated
        }
    }
}
