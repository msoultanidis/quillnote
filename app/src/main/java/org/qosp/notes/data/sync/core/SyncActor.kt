@file: OptIn(ObsoleteCoroutinesApi::class)

package org.qosp.notes.data.sync.core

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.first
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.repo.IdMappingRepository
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.preferences.CloudService
import java.time.Instant

class SyncActor(
    private val noteRepository: NoteRepository,
    private val idMappingRepository: IdMappingRepository,
) {

    fun launchIn(coroutineScope: CoroutineScope): SendChannel<Message> {
        return coroutineScope.actor {
            var lastSyncFinished = Instant.now().epochSecond

            for (msg in channel) {
                with(msg) {
                    val result = when (this) {
                        is CreateNote -> createNote(note, provider, config)
                        is UpdateNote -> {
                            if (note.modifiedDate <= lastSyncFinished) {
                                MutationWhileSyncing()
                            } else {
                                ifMappingExists(note, config.provider) {
                                    updateNote(note, it, provider, config)
                                }
                            }
                        }
                        is UpdateOrCreateNote -> {
                            val mapping = idMappingRepository.getByLocalIdAndProvider(note.id, config.provider)
                            if (mapping != null) {
                                if (note.modifiedDate <= lastSyncFinished) {
                                    MutationWhileSyncing()
                                } else {
                                    ifMappingExists(note, config.provider) {
                                        updateNote(note, it, provider, config)
                                    }
                                }
                            } else {
                                createNote(note, provider, config)
                            }
                        }
                        is DeleteNote -> ifMappingExists(note, config.provider) {
                            deleteNote(note, it, provider, config)
                        }
                        is DeleteNotes -> {
                            deleteNotes(notes, provider, config)
                        }
                        is MoveNoteToBin -> ifMappingExists(note, config.provider) {
                            moveNoteToBin(note, it, provider, config)
                        }
                        is RestoreNote -> restoreNote(note, provider, config)
                        is Sync -> {
                            sync(provider, config).also {
                                if (it is Success) {
                                    lastSyncFinished = Instant.now().epochSecond
                                }
                            }
                        }
                        is Authenticate -> authenticate(provider, config)
                        is IsServerCompatible -> isServerCompatible(provider, config)
                    }

                    deferred.complete(result)
                }
            }
        }
    }

    private suspend inline fun <T> ifMappingExists(
        note: Note,
        provider: CloudService,
        block: (IdMapping) -> Response<T>,
    ): Response<T> {
        val mapping = idMappingRepository.getByLocalIdAndProvider(note.id, provider)
        return if (mapping != null) block(mapping) else GenericError()
    }

    private suspend fun createNote(note: Note, provider: SyncProvider, config: ProviderConfig): Response<RemoteNote> {
        val remoteNote = provider.createNote(note, config).bodyOrElse { return it }
        idMappingRepository.createMappingForNote(note, remoteNote)

        return Success(remoteNote)
    }

    private suspend fun updateNote(note: Note, mapping: IdMapping, provider: SyncProvider, config: ProviderConfig): Response<RemoteNote> {
        val remoteNote = provider.updateNote(note, config, mapping).bodyOrElse { return it }
        idMappingRepository.update(note, remoteNote)

        return Success(remoteNote)
    }

    private suspend fun deleteNote(note: Note, mapping: IdMapping, provider: SyncProvider, config: ProviderConfig): Response<RemoteNote> {
        val remoteNote = provider.deleteNote(note, config, mapping).bodyOrElse { return it }
        idMappingRepository.deleteMappingsOfRemoteNote(config.provider, remoteNote.id)

        return Success(remoteNote)
    }

    private suspend fun deleteByRemoteId(provider: SyncProvider, config: ProviderConfig, vararg remoteIds: Long): Response<Any> {
        provider.deleteByRemoteId(config, *remoteIds)
        idMappingRepository.deleteMappingsOfRemoteNote(config.provider, *remoteIds)

        return Success()
    }

    private suspend fun deleteNotes(notes: List<Note>, provider: SyncProvider, config: ProviderConfig): Response<Any> {
        val ids = notes.mapNotNull {
            idMappingRepository.getByLocalIdAndProvider(it.id, config.provider)?.remoteNoteId
        }

        provider.deleteByRemoteId(config, *ids.toLongArray())
        idMappingRepository.deleteMappingsForNotes(config.provider, *notes.toTypedArray())

        return Success()
    }

    private suspend fun moveNoteToBin(note: Note, mapping: IdMapping, provider: SyncProvider, config: ProviderConfig): Response<RemoteNote> {
        val response = provider.moveNoteToBin(note, config, mapping)
        return when {
            response is Success && response.body != null -> {
                idMappingRepository.update(note, response.body)
                Success(response.body)
            }
            response is OperationNotSupported -> deleteNote(note, mapping, provider, config)
            else -> GenericError()
        }
    }

    private suspend fun restoreNote(note: Note, provider: SyncProvider, config: ProviderConfig): Response<RemoteNote> {
        return when {
            provider.supportsBin() -> {
                val response = ifMappingExists(note, config.provider) {
                    provider.restoreNote(note, config, it)
                }

                when {
                    response is Success && response.body != null -> {
                        idMappingRepository.update(note, response.body)
                        Success(response.body)
                    }
                    else -> GenericError()
                }
            }
            else -> createNote(note, provider, config)
        }
    }

    private suspend fun authenticate(provider: SyncProvider, config: ProviderConfig): Response<Any> {
        return provider.authenticate(config)
    }

    private suspend fun isServerCompatible(provider: SyncProvider, config: ProviderConfig): Response<Any> {
        return provider.isServerCompatible(config)
    }

    private suspend fun sync(provider: SyncProvider, config: ProviderConfig): Response<Any> {
        suspend fun RemoteNote.asLocalNote() = with(provider) { toLocalNote() }

        val timeSyncStarted = Instant.now().epochSecond
        val response = provider.getAll(config)
        val remoteNotes = response.bodyOrElse { return GenericError() }
            .groupBy { it.id }
            .toMutableMap()
        val localNotes = noteRepository
            .getAll()
            .first()
            .filterNot { it.isLocalOnly || it.isDeleted || it.creationDate > timeSyncStarted }
            .groupBy { it.id }
            .toMutableMap()

        // Clean up the mappings table by removing mappings for local or remote notes that do not exist
        idMappingRepository.deleteIfIdsNotIn(
            localIds = localNotes.keys.toList() - SyncProvider.NOT_SET_ID,
            remoteIds = remoteNotes.keys.toList() - SyncProvider.NOT_SET_ID,
        )

        val remoteIdsToBeDeleted: List<Long>
        val mappings = idMappingRepository.getAllByProvider(config.provider).let { allMappings ->
            val mappingsToBeDeleted = allMappings.filter { it.localNoteId == null }

            remoteIdsToBeDeleted = mappingsToBeDeleted.map { it.remoteNoteId }

            // Return only the mappings with non-null localNoteId
            allMappings - mappingsToBeDeleted
        }

        deleteByRemoteId(provider, config, *remoteIdsToBeDeleted.toLongArray())

        val notesToBeUpdated = mutableListOf<Note>()
        val mappingsToBeUpdated = mutableListOf<IdMapping>()
        val notesToBeMovedToBin = mutableListOf<Note>()

        for (mapping in mappings) {
            val localNote = localNotes.remove(mapping.localNoteId)?.first() ?: continue
            val remoteNote = remoteNotes.remove(mapping.remoteNoteId)?.first()

            if (remoteNote == null) {
                // Note was deleted remotely. We should move the note to the bin in case the bin
                // in case the user wants to restore it later
                notesToBeMovedToBin.add(localNote)
                continue
            }

            when (remoteNote.compareTo(localNote, mapping)) {
                RemoteNote.IsNewer -> {
                    notesToBeUpdated.add(remoteNote.asLocalNote())
                    mappingsToBeUpdated.add(mapping.copy(extras = remoteNote.extras))
                }
                RemoteNote.IsOutdated -> updateNote(localNote, mapping, provider, config)
                RemoteNote.IsSame -> continue
            }
        }

        noteRepository.updateNotes(*notesToBeUpdated.toTypedArray())
        idMappingRepository.updateMappings(*mappingsToBeUpdated.toTypedArray())
        noteRepository.moveNotesToBin(*notesToBeMovedToBin.toTypedArray())
        idMappingRepository.deleteMappingsForNotes(config.provider, *notesToBeMovedToBin.toTypedArray())

        // Persist new notes created remotely
        remoteNotes.values.flatten().forEach { remoteNote ->
            val localNote = remoteNote.asLocalNote().let { it.copy(id = noteRepository.insertNote(it)) }
            if (remoteNote.id == SyncProvider.NOT_SET_ID) provider.assignIdToNote(remoteNote, localNote.id, config)
            idMappingRepository.createMappingForNote(localNote, remoteNote)
        }


        // Upload new local notes
        localNotes.values.flatten().forEach {
            createNote(it, provider, config)
        }

        return Success()
    }

    sealed class Message(val provider: SyncProvider, val config: ProviderConfig) {
        val deferred: CompletableDeferred<Response<*>> = CompletableDeferred()
        open val note: Note? = null
    }
    class CreateNote(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class UpdateNote(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class UpdateOrCreateNote(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class DeleteNote(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class DeleteNotes(val notes: List<Note>, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class RestoreNote(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class MoveNoteToBin(override val note: Note, provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class Sync(provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class Authenticate(provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
    class IsServerCompatible(provider: SyncProvider, config: ProviderConfig) : Message(provider, config)
}
