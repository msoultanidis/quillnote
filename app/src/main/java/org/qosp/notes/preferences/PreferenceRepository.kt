package org.qosp.notes.preferences

import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.runCatching
import com.tfcporciuncula.flow.FlowSharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.io.IOException

val Context.dataStore by preferencesDataStore("preferences")

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceRepository(context: Context) {
    val dataStore = context.dataStore

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences by lazy {
        FlowSharedPreferences(
            EncryptedSharedPreferences.create(
                context,
                "encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        )
    }

    fun getEncryptedString(key: String): Flow<String> {
        return sharedPreferences.getString(key, "").asFlow()
    }

    suspend fun putEncryptedStrings(vararg pairs: Pair<String, String>) {
        pairs.forEach { (key, value) ->
            sharedPreferences.getString(key).setAndCommit(value)
        }
    }

    companion object {
        const val NEXTCLOUD_INSTANCE_URL = "NEXTCLOUD_INSTANCE_URL"
        const val NEXTCLOUD_USERNAME = "NEXTCLOUD_USERNAME"
        const val NEXTCLOUD_PASSWORD = "NEXTCLOUD_PASSWORD"
    }
}

inline fun <reified T> PreferenceRepository.get(): Flow<T> where T : Enum<T>, T : Preference<T> {
    val first = runCatching { enumValues<T>().first() }
    return first.mapBoth(
        success = { first ->
            dataStore.data
                .catch {
                    if (it is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw it
                    }
                }
                .map { pref ->
                    preferenceOf<T>(pref[first.getPreferenceKey()]) ?: first
                }
        },
        failure = { flow {} },
    )
}

suspend fun <T> PreferenceRepository.set(preference: T) where T : Enum<T>, T : Preference<T> {
    dataStore.edit { preferences ->
        preferences[preference.getPreferenceKey()] = preference.name
    }
}
