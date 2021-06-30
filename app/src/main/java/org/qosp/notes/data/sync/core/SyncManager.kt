package org.qosp.notes.data.sync.core

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.sync.nextcloud.NextcloudConfig
import org.qosp.notes.data.sync.nextcloud.NextcloudManager
import org.qosp.notes.preferences.CloudService
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.preferences.SyncMode
import org.qosp.notes.ui.utils.ConnectionManager

class SyncManager(
    private val nextcloudManager: NextcloudManager,
    private val preferenceRepository: PreferenceRepository,
    val connectionManager: ConnectionManager,
    val syncingScope: CoroutineScope,
    syncActor: SyncActor,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val prefs: Flow<SyncPrefs> = preferenceRepository.getAll().flatMapLatest { prefs ->
        when (prefs.cloudService) {
            CloudService.DISABLED -> flowOf(SyncPrefs(false, null, prefs.syncMode, null))
            CloudService.NEXTCLOUD -> {
                NextcloudConfig.fromPreferences(preferenceRepository).map { config ->
                    SyncPrefs(true, nextcloudManager, prefs.syncMode, config)
                }
            }
        }
    }

    val config = prefs.map { prefs -> prefs.config }

    private val actor = syncActor.launchIn(syncingScope)

    suspend inline fun <R> ifSyncing(
        customConfig: ProviderConfig? = null,
        block: (SyncProvider, ProviderConfig) -> Response<R>,
    ): Response<R> {
        val (isEnabled, provider, mode, prefConfig) = prefs.first()
        val config = customConfig ?: prefConfig

        return when {
            !isEnabled -> SyncingNotEnabled()
            provider == null || config == null -> InvalidConfig()
            !connectionManager.isConnectionAvailable(mode) -> NoConnectivity()
            else -> block(provider, config)
        }
    }

    private suspend inline fun <R> sendMessage(
        customConfig: ProviderConfig? = null,
        crossinline block: suspend (SyncProvider, ProviderConfig) -> SyncActor.Message,
    ): Response<R> {
        return ifSyncing(customConfig) { provider, config ->
            val message = block(provider, config)
            actor.send(message)

            @Suppress("UNCHECKED_CAST") // Ssshh.
            message.deferred.await() as Response<R>
        }
    }

    suspend fun sync(): Response<Any> = sendMessage { provider, config ->
        SyncActor.Sync(provider, config)
    }

    suspend fun createNote(note: Note): Response<RemoteNote> = sendMessage { provider, config ->
        SyncActor.CreateNote(note, provider, config)
    }

    suspend fun deleteNote(note: Note): Response<Any> = sendMessage { provider, config ->
        SyncActor.DeleteNote(note, provider, config)
    }

    suspend fun moveNoteToBin(note: Note): Response<Any> = sendMessage { provider, config ->
        SyncActor.MoveNoteToBin(note, provider, config)
    }

    suspend fun restoreNote(note: Note): Response<Any> = sendMessage { provider, config ->
        SyncActor.RestoreNote(note, provider, config)
    }

    suspend fun updateNote(note: Note): Response<RemoteNote> = sendMessage { provider, config ->
        SyncActor.UpdateNote(note, provider, config)
    }

    suspend fun updateOrCreate(note: Note): Response<RemoteNote> = sendMessage { provider, config ->
        SyncActor.UpdateOrCreateNote(note, provider, config)
    }

    suspend fun isServerCompatible(customConfig: ProviderConfig? = null): Response<Any> = sendMessage(customConfig) { provider, config ->
        SyncActor.IsServerCompatible(provider, config)
    }

    suspend fun authenticate(customConfig: ProviderConfig? = null): Response<Any> = sendMessage(customConfig) { provider, config ->
        SyncActor.Authenticate(provider, config)
    }
}

data class SyncPrefs(
    val isEnabled: Boolean,
    val provider: SyncProvider?,
    val mode: SyncMode,
    val config: ProviderConfig?,
)
