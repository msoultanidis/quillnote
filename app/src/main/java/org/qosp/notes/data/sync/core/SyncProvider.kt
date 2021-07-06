package org.qosp.notes.data.sync.core

import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note

interface SyncProvider {
    suspend fun getAll(config: ProviderConfig): Response<List<RemoteNote>>

    suspend fun createNote(note: Note, config: ProviderConfig): Response<RemoteNote>
    suspend fun deleteNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote>
    suspend fun deleteByRemoteId(config: ProviderConfig, vararg remoteIds: Long): Response<Any>
    suspend fun updateNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote>

    suspend fun supportsBin(): Boolean
    suspend fun moveNoteToBin(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote>
    suspend fun restoreNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote>

    suspend fun authenticate(config: ProviderConfig): Response<Any>
    suspend fun isServerCompatible(config: ProviderConfig): Response<Any>

    suspend fun RemoteNote.toLocalNote(old: Note? = null): Note
    suspend fun Note.toRemoteNote(old: RemoteNote? = null): RemoteNote
}
