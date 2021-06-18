package org.qosp.notes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.qosp.notes.components.workers.BinCleaningWorker
import org.qosp.notes.data.UsesTestDatabase
import org.qosp.notes.data.database
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class BinCleaningWorkerTest : UsesTestDatabase {
    private lateinit var context: Context
    private lateinit var worker: BinCleaningWorker

    @Before
    fun prepare() {

        context = ApplicationProvider.getApplicationContext()

        // TODO: Make everything testable :(
//        val factory = object : WorkerFactory() {
//            override fun createWorker(
//                appContext: Context,
//                workerClassName: String,
//                workerParameters: WorkerParameters
//            ): ListenableWorker? {
//                return BinCleaningWorker(
//                    context = appContext,
//                    params = workerParameters,
//                    noteRepository = (),
//                    storageCleaner = (),
//                )
//            }
//        }
//
//        val worker = TestListenableWorkerBuilder<BinCleaningWorker>(context).setWorkerFactory(factory).build()
    }

    @After
    @Throws(IOException::class)
    fun cleanUp() {
        database.close()
    }

    @Test
    @Throws(Exception::class)
    fun workerShouldPermanentlyDeleteNotesInBin() = runBlocking {
        val result = worker.doWork()
    }
}
