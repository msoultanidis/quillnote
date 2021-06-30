package org.qosp.notes.data.sync.nextcloud.model

import kotlinx.serialization.Serializable
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.core.RemoteNote
import org.qosp.notes.preferences.CloudService
import java.time.Instant

@Serializable
data class NextcloudNote(
    override val id: Long,
    val etag: String? = null,
    val content: String = "",
    val title: String = "",
    val category: String = "",
    val favorite: Boolean = false,
    val modified: Long = Instant.now().epochSecond,
    val readOnly: Boolean? = null,
) : RemoteNote {

    override val provider: CloudService = CloudService.NEXTCLOUD
    override val extras: String? = etag

    override fun compareTo(localNote: Note, mapping: IdMapping): RemoteNote.Comparison {
        val haveSameDateModified = (modified == localNote.modifiedDate && favorite == localNote.isPinned)

        return when {
            etag == mapping.extras && haveSameDateModified -> RemoteNote.IsSame
            modified < localNote.modifiedDate -> RemoteNote.IsOutdated
            else -> RemoteNote.IsNewer
        }
    }
}
