package org.qosp.notes.data.sync.local

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import org.qosp.notes.data.sync.core.ConfigFactory
import org.qosp.notes.data.sync.core.ProviderConfig
import org.qosp.notes.preferences.CloudService
import org.qosp.notes.preferences.PreferenceRepository

data class LocalProviderConfig(
    val dir: DocumentFile,
    override val username: String,
) : ProviderConfig {
    override val authenticationHeaders: Map<String, String> = mapOf()
    override val provider: CloudService = CloudService.LOCAL
}

@OptIn(ExperimentalCoroutinesApi::class)
fun ConfigFactory.createLocalConfig(): Flow<LocalProviderConfig?> {
    val uri = preferenceRepository.getEncryptedString(PreferenceRepository.LOCAL_SYNC_DIRECTORY_PATH)

    return uri.map { uriString ->
        if (uriString.isEmpty()) return@map null

        DocumentFile.fromTreeUri(context, Uri.parse(uriString))
            ?.let { LocalProviderConfig(it, "Local") }
            .takeIf { it?.dir?.exists() == true }
    }
}