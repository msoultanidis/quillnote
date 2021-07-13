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

    suspend fun deleteMappingsForNotes(provider: CloudService, vararg notes: Note) {
        idMappingDao.deleteIfLocalIdIn(provider, notes.map { it.id })
    }

    suspend fun unassignNotesFromProviders(vararg notes: Note) {
        idMappingDao.unassignNotesFromProviders(notes.map { it.id })
    }

    suspend fun deleteMappingsOfRemoteNote(provider: CloudService, vararg remoteIds: Long) {
        idMappingDao.deleteMappingsOfRemoteNote(provider, *remoteIds)
    }

    suspend fun deleteIfIdsNotIn(localIds: List<Long>, remoteIds: List<Long>) {
        idMappingDao.deleteIfIdsNotIn(localIds, remoteIds)
    }

    suspend fun getAllByLocalId(localId: Long): List<IdMapping> {
        return idMappingDao.getAllByLocalId(localId)
    }

    suspend fun insertMapping(mapping: IdMapping) {
        idMappingDao.insertMapping(mapping)
    }
}
