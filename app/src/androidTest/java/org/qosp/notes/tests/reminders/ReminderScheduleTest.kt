package org.qosp.notes.tests.reminders

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.Reminder
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.data.repo.ReminderRepository
import org.qosp.notes.di.NO_SYNC
import org.qosp.notes.ui.reminders.ReminderManager
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidTest
class ReminderScheduleTest {
    @Inject
    lateinit var reminderManager: ReminderManager

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    @Throws(Exception::class)
    fun reminderIsScheduledCorrectly()  {
        val (reminderId, noteId) = 1L to 1L
        reminderManager.schedule(reminderId, Instant.now().plusSeconds(3600).epochSecond, noteId)
        assertTrue(reminderManager.isReminderSet(reminderId, noteId))
    }
}
