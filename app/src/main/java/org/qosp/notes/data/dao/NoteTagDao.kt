package org.qosp.notes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.NoteTagJoin
import org.qosp.notes.data.model.Tag

@Dao
interface NoteTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg joins: NoteTagJoin)

    @Delete
    suspend fun delete(vararg joins: NoteTagJoin)

    @Query(
        """
        SELECT tags.name, tags.id FROM tags 
        INNER JOIN note_tags ON tags.id = note_tags.tagId 
        WHERE note_tags.noteId = :noteId
        """
    )
    fun getByNoteId(noteId: Long): Flow<List<Tag>>

    @Query(
        """
        INSERT INTO note_tags (tagId, noteId)
        SELECT tagId, :toNoteId FROM note_tags WHERE noteId = :fromNoteId
        """
    )
    suspend fun copyTags(fromNoteId: Long, toNoteId: Long)

    @Query("SELECT * FROM note_tags")
    fun getAllNoteTagRelations(): Flow<List<NoteTagJoin>>

    @RewriteQueriesToDropUnusedColumns
    @Transaction
    @Query(
        """
        SELECT * FROM notes 
        INNER JOIN note_tags ON notes.id = note_tags.noteId 
        WHERE note_tags.tagId = :tagId
        """
    )
    fun getNotesByTagId(tagId: Long): Flow<List<Note>>
}
