package org.qosp.notes.ui.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.addRepeatingJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

inline fun <T> Flow<T>.collect(lifecycleOwner: LifecycleOwner, crossinline action: suspend (value: T) -> Unit) {
    lifecycleOwner.addRepeatingJob(Lifecycle.State.STARTED) {
        this@collect.collect(action)
    }
}
