package org.qosp.notes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.qosp.notes.data.model.Tag

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Update
    suspend fun update(vararg tags: Tag)

    @Delete
    suspend fun delete(vararg tags: Tag)

    @Query("SELECT * FROM tags")
    fun getAll(): Flow<List<Tag>>

    @Query("SELECT * FROM tags WHERE id = :tagId LIMIT 1")
    fun getById(tagId: Long): Flow<Tag?>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    fun getByName(name: String): Flow<Tag?>
}
