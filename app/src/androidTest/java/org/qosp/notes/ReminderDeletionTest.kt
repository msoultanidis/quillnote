package org.qosp.notes

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.qosp.notes.data.UsesTestDatabase
import org.qosp.notes.data.dao.NoteDao
import org.qosp.notes.data.dao.ReminderDao
import org.qosp.notes.data.database
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.Reminder
import java.io.IOException
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class ReminderDeletionTest : UsesTestDatabase {

    private lateinit var noteDao: NoteDao
    private lateinit var reminderDao: ReminderDao

    @Before
    fun prepare() {
        noteDao = database.noteDao
        reminderDao = database.reminderDao
    }

    @After
    @Throws(IOException::class)
    fun cleanUp() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun deletingNoteShouldDeleteReminderToo() = runBlocking {
        val note = Note(
            title = "Test Note",
            content = "Sample content"
        )
        val noteId = noteDao.insert(note.toEntity())

        val reminder = Reminder(
            name = "Test Reminder",
            noteId = noteId,
            date = Instant.now().epochSecond
        )

        reminderDao.insert(reminder)
        noteDao.delete(note.copy(id = noteId).toEntity())

        assertThat(reminderDao.getAll().first().isEmpty(), equalTo(true))
    }
}
