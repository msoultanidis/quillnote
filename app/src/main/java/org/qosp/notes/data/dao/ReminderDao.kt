package org.qosp.notes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.qosp.notes.data.model.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(vararg reminders: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reminders WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Long)

    @Query("DELETE FROM reminders WHERE noteId IN (:ids)")
    suspend fun deleteIfNoteIdIn(ids: List<Long>)

    @Query("SELECT * FROM reminders")
    fun getAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE noteId = :noteId")
    fun getByNoteId(noteId: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    fun getById(reminderId: Long): Flow<Reminder?>

    @Query(
        """
    INSERT INTO reminders (name, noteId, date)
    SELECT name, :toNoteId, date FROM reminders WHERE noteId = :fromNoteId
    """
    )
    suspend fun copyReminders(fromNoteId: Long, toNoteId: Long)
}
