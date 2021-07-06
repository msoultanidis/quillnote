package org.qosp.notes.data.repo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.msoul.datastore.defaultOf
import org.qosp.notes.data.dao.NoteDao
import org.qosp.notes.data.dao.ReminderDao
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.core.SyncManager
import org.qosp.notes.preferences.SortMethod
import java.time.Instant

class NoteRepository(
    private val noteDao: NoteDao,
    private val idMappingRepository: IdMappingRepository,
    private val reminderDao: ReminderDao,
    private val syncManager: SyncManager?,
) {

    private suspend fun cleanMappingsForLocalNotes(vararg notes: Note) {
        notes
            .filter { it.isLocalOnly }
            .also { idMappingRepository.unassignNotesFromProviders(*it.toTypedArray()) }
    }

    suspend fun insertNote(note: Note, shouldSync: Boolean = true): Long {
        val id = noteDao.insert(note.toEntity())

        if (!note.isLocalOnly && shouldSync && syncManager != null) {
            syncManager.syncingScope.launch {
                syncManager.createNote(note.copy(id = id))
            }
        }

        return id
    }

    suspend fun updateNotes(vararg notes: Note, shouldSync: Boolean = true) {
        val array = notes
            .map { it.toEntity() }
            .toTypedArray()
        noteDao.update(*array)

        if (shouldSync && syncManager != null) {
            syncManager.syncingScope.launch {
                notes
                    .asSequence()
                    .filterNot { it.isLocalOnly }
                    .forEach { syncManager.updateOrCreate(it) }
            }
        }
    }

    suspend fun moveNotesToBin(vararg notes: Note, shouldSync: Boolean = true) {
        val array = notes
            .map { it.toEntity().copy(isDeleted = true, deletionDate = Instant.now().epochSecond) }
            .toTypedArray()
        noteDao.update(*array)

        reminderDao.deleteIfNoteIdIn(notes.map { it.id })
        cleanMappingsForLocalNotes(*notes)

        if (shouldSync && syncManager != null) {
            syncManager.syncingScope.launch {
                notes
                    .asSequence()
                    .filterNot { it.isLocalOnly }
                    .forEach { syncManager.moveNoteToBin(it) }
            }
        }
    }

    suspend fun restoreNotes(vararg notes: Note, shouldSync: Boolean = true) {
        val array = notes
            .map { it.toEntity().copy(isDeleted = false, deletionDate = null) }
            .toTypedArray()
        noteDao.update(*array)

        cleanMappingsForLocalNotes(*notes)

        if (shouldSync && syncManager != null) {
            syncManager.syncingScope.launch {
                notes
                    .asSequence()
                    .filterNot { it.isLocalOnly }
                    .forEach { syncManager.restoreNote(it) }
            }
        }
    }

    suspend fun deleteNotes(vararg notes: Note, shouldSync: Boolean = true) {
        val array = notes
            .map { it.toEntity() }
            .toTypedArray()
        noteDao.delete(*array)

        idMappingRepository.unassignNotesFromProviders(*notes)

        if (shouldSync && syncManager != null) {
            syncManager.syncingScope.launch {
                syncManager.deleteNotes(
                    *notes
                        .filterNot { it.isLocalOnly }
                        .toTypedArray()
                )
            }
        }
    }

    suspend fun discardEmptyNotes(): Boolean {
        val notes = noteDao.getAll(defaultOf())
            .first()
            .filter { it.isEmpty() }
            .toTypedArray()

        return notes.isNotEmpty().also {
            if (it) deleteNotes(*notes)
        }
    }

    suspend fun permanentlyDeleteNotesInBin() {
        val notes = noteDao.getDeleted(defaultOf()).first().toTypedArray()
        idMappingRepository.unassignNotesFromProviders(*notes)

        noteDao.permanentlyDeleteNotesInBin()
    }

    fun getById(noteId: Long): Flow<Note?> {
        return noteDao.getById(noteId)
    }

    fun getDeleted(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getDeleted(sortMethod)
    }

    fun getArchived(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getArchived(sortMethod)
    }

    fun getNonDeleted(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getNonDeleted(sortMethod)
    }

    fun getNonDeletedOrArchived(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getNonDeletedOrArchived(sortMethod)
    }

    fun getAll(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getAll(sortMethod)
    }

    fun getByNotebook(notebookId: Long, sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getByNotebook(notebookId, sortMethod)
    }

    fun getNotesWithoutNotebook(sortMethod: SortMethod = defaultOf()): Flow<List<Note>> {
        return noteDao.getNotesWithoutNotebook(sortMethod)
    }

}
