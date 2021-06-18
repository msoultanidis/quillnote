package org.qosp.notes.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.qosp.notes.data.model.*
import org.qosp.notes.data.repo.*

@Serializable
data class Backup(
    val version: Int,
    val notes: Set<Note> = setOf(),
    val notebooks: Set<Notebook> = setOf(),
    val reminders: Set<Reminder> = setOf(),
    val tags: Set<Tag> = setOf(),
    val joins: Set<NoteTagJoin> = setOf(),
    val idMappings: Set<IdMapping> = setOf(),
) {
    fun serialize() = Json.encodeToString(this)

    companion object {
        fun fromString(serialized: String): Backup = Json.decodeFromString(serialized)
    }
}
