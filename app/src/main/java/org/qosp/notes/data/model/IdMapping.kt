package org.qosp.notes.data.model

import androidx.room.Entity
import kotlinx.serialization.Serializable
import org.qosp.notes.preferences.CloudService

// @Serializable
// @Entity(tableName = "cloud_ids")
// data class IdMapping(
//    @PrimaryKey(autoGenerate = true)
//    val mappingId: Long = 0L,
//    val localNoteId: Long,
//    val remoteNoteId: Long?,
//    val provider: CloudService?,
//    val extras: String?,
//    val isDeletedLocally: Boolean,
//    val isBeingUpdated: Boolean = false,
// )

@Serializable
@Entity(
    tableName = "id_mappings",
    primaryKeys = ["remoteNoteId", "provider"]
)
data class IdMapping(
    val localNoteId: Long?,
    val remoteNoteId: Long, // PRIMARY
    val provider: CloudService, // PRIMARY
    val extras: String?,
)

// You create the mapping only for notes that are synced
// Local notes will never have mappings

//
