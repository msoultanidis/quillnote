package org.qosp.notes.data.sync.core

import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.preferences.CloudService

interface RemoteNote {
    val id: Long
    val extras: String?
    val provider: CloudService

    fun compareTo(localNote: Note, mapping: IdMapping): Comparison

    sealed class Comparison
    object IsOutdated : Comparison()
    object IsNewer : Comparison()
    object IsSame : Comparison()
}
