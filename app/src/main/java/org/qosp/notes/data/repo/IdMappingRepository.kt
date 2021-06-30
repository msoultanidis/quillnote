package org.qosp.notes.data.repo

import org.qosp.notes.data.dao.IdMappingDao
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.core.RemoteNote
import org.qosp.notes.preferences.CloudService

class IdMappingRepository(private val idMappingDao: IdMappingDao) {

    suspend fun updateMappings(vararg mappings: IdMapping) {
        idMappingDao.update(*mappings)
    }

    suspend fun update(note: Note, remoteNote: RemoteNote) {
        idMappingDao.update(remoteNote.id, remoteNote.provider, remoteNote.extras)
    }

    suspend fun getAllByProvider(provider: CloudService): List<IdMapping> {
        return idMappingDao.getAllByProvider(provider)
    }

    suspend fun getByLocalIdAndProvider(localId: Long, provider: CloudService): IdMapping? {
        return idMappingDao.getByLocalIdAndProvider(localId, provider)
    }

    suspend fun createMappingForNote(note: Note, remoteNote: RemoteNote) {
        idMappingDao.insertMapping(
            IdMapping(
                localNoteId = note.id,
                remoteNoteId = remoteNote.id,
                extras = remoteNote.extras,
                provider = remoteNote.provider,
            )
        )
    }

    suspend fun unassignLocalNotesFromProvider(provider: CloudService, vararg notes: Note) {
        idMappingDao.unassignLocalNotesFromProvider(provider, notes.map { it.id })
    }

    suspend fun unassignNotesFromProviders(vararg notes: Note) {
        idMappingDao.unassignNotesFromProviders(notes.map { it.id })
    }

    suspend fun deleteMappingsOfRemoteNote(remoteNote: RemoteNote) {
        idMappingDao.deleteMappingsOfRemoteNote(remoteNote.id, remoteNote.provider)
    }

    suspend fun deleteIfLocalIdNotIn(ids: List<Long>) {
        idMappingDao.deleteIfLocalIdNotIn(ids)
    }

    suspend fun getAllByLocalId(localId: Long): List<IdMapping> {
        return idMappingDao.getAllByLocalId(localId)
    }

    suspend fun insertMapping(mapping: IdMapping) {
        idMappingDao.insertMapping(mapping)
    }
}
