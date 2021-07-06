package org.qosp.notes.data.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.flow.Flow
import org.qosp.notes.data.model.*
import org.qosp.notes.preferences.SortMethod
import org.qosp.notes.preferences.SortMethod.*

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(vararg notes: NoteEntity)

    @Delete
    suspend fun delete(vararg notes: NoteEntity)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun permanentlyDeleteNotesInBin()

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun getById(noteId: Long): Flow<Note?>

    @Transaction
    @RawQuery(
        observedEntities = [
            NoteEntity::class,
            Tag::class,
            Reminder::class,
            NoteTagJoin::class,
        ]
    )
    fun rawGetQuery(query: SimpleSQLiteQuery): Flow<List<Note>>

    fun getDeleted(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isDeleted = 1 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    fun getArchived(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    fun getNonDeleted(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isDeleted = 0 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    fun getNonDeletedOrArchived(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isArchived = 0 AND isDeleted = 0 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    fun getAll(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    fun getByNotebook(notebookId: Long, sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isArchived = 0 AND isDeleted = 0 AND notebookId = $notebookId 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }

    private fun getOrderByMethod(sortMethod: SortMethod): Pair<String, String> {
        val column = when (sortMethod) {
            TITLE_ASC, TITLE_DESC -> "title"
            CREATION_ASC, CREATION_DESC -> "creationDate"
            MODIFIED_ASC, MODIFIED_DESC -> "modifiedDate"
        }
        val order = when (sortMethod) {
            TITLE_ASC, CREATION_ASC, MODIFIED_ASC -> "ASC"
            TITLE_DESC, CREATION_DESC, MODIFIED_DESC -> "DESC"
        }
        return Pair(column, order)
    }

    fun getNotesWithoutNotebook(sortMethod: SortMethod): Flow<List<Note>> {
        val (column, order) = getOrderByMethod(sortMethod)
        return rawGetQuery(
            SimpleSQLiteQuery(
                """
                SELECT * FROM notes WHERE isArchived = 0 AND isDeleted = 0 AND notebookId IS NULL 
                ORDER BY isPinned DESC, $column $order
            """
            )
        )
    }
}
