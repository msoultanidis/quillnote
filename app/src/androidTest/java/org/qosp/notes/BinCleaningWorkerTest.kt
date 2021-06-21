package org.qosp.notes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.qosp.notes.components.MediaStorageManager
import org.qosp.notes.components.workers.BinCleaningWorker
import org.qosp.notes.data.UsesTestDatabase
import org.qosp.notes.data.database
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.preferences.NoteDeletionTime
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.preferences.set
import java.io.File
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BinCleaningWorkerTest : UsesTestDatabase { // TODO: Use Hilt for tests
    private lateinit var worker: BinCleaningWorker
    private lateinit var context: Context
    private lateinit var noteRepository: NoteRepository
    private lateinit var preferenceRepository: PreferenceRepository
    private val mediaFolder = "worker_test"

    @Before
    fun prepare() {
        context = ApplicationProvider.getApplicationContext()
        noteRepository = NoteRepository(database.noteDao, database.idMappingDao, null)
        preferenceRepository = PreferenceRepository(context)

        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return BinCleaningWorker(
                    context = appContext,
                    params = workerParameters,
                    preferenceRepository = preferenceRepository,
                    noteRepository = noteRepository,
                    mediaStorageManager = MediaStorageManager(appContext, noteRepository, mediaFolder = mediaFolder),
                )
            }
        }

        worker = TestListenableWorkerBuilder<BinCleaningWorker>(context)
            .setWorkerFactory(factory)
            .build()
    }

    @After
    @Throws(IOException::class)
    fun cleanUp() {
        database.close()
        File(context.filesDir, mediaFolder).deleteRecursively()
    }

    @Test
    @Throws(Exception::class)
    fun workerShouldPermanentlyDeleteNotesInBin() = runBlocking {
        preferenceRepository.set(NoteDeletionTime.WEEK)
    }
}
