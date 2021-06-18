package org.qosp.notes.preferences

import androidx.datastore.preferences.core.stringPreferencesKey
import com.github.michaelbull.result.get
import com.github.michaelbull.result.runCatching

inline fun <reified T : Enum<T>> key(key: String): Preference<T> {
    return object : Preference<T> {
        override val key = key
        override val nameResource = 0
    }
}

interface Preference<T : Enum<T>> {
    val key: String
    val nameResource: Int
    fun getPreferenceKey() = stringPreferencesKey(key)
}

inline fun <reified T : Enum<T>> preferenceOf(value: String?): T? {
    return if (value == null) null else runCatching { enumValueOf<T>(value) }.get()
}
