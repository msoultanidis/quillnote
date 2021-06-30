package org.qosp.notes.data.dao

import androidx.room.*
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.preferences.CloudService

@Dao
interface IdMappingDao {
    @Update
    suspend fun update(vararg mappings: IdMapping)

    @Query("UPDATE id_mappings SET extras = :extras WHERE remoteNoteId = :remoteNoteId AND provider = :provider")
    suspend fun update(remoteNoteId: Long, provider: CloudService, extras: String?)

    @Query("SELECT * FROM id_mappings WHERE provider = :provider")
    suspend fun getAllByProvider(provider: CloudService): List<IdMapping>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapping(idMapping: IdMapping)

    @Query("UPDATE id_mappings SET localNoteId = NULL WHERE localNoteId IN (:ids)")
    suspend fun unassignNotesFromProviders(ids: List<Long>)

    @Query("DELETE FROM id_mappings WHERE remoteNoteId = :remoteNoteId AND provider = :provider")
    suspend fun deleteMappingsOfRemoteNote(remoteNoteId: Long, provider: CloudService)

    @Query("SELECT * FROM id_mappings WHERE localNoteId = :localId AND provider = :provider")
    suspend fun getByLocalIdAndProvider(localId: Long, provider: CloudService): IdMapping?

    @Query("UPDATE id_mappings SET localNoteId = NULL WHERE localNoteId IN (:ids) AND provider = :provider")
    suspend fun unassignLocalNotesFromProvider(provider: CloudService, ids: List<Long>)

    @Query("DELETE FROM id_mappings WHERE localNoteId NOT IN (:ids)")
    suspend fun deleteIfLocalIdNotIn(ids: List<Long>)

    @Query("SELECT * FROM id_mappings WHERE localNoteId = :localId")
    suspend fun getAllByLocalId(localId: Long): List<IdMapping>
}
