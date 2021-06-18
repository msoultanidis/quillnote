package org.qosp.notes.components.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.qosp.notes.components.StorageCleaner
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.preferences.NoteDeletionTime
import org.qosp.notes.preferences.PreferenceRepository
import org.qosp.notes.preferences.SortMethod
import org.qosp.notes.preferences.get
import java.time.Instant
import java.util.concurrent.TimeUnit

@HiltWorker
class BinCleaningWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferenceRepository: PreferenceRepository,
    private val noteRepository: NoteRepository,
    private val storageCleaner: StorageCleaner,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val deletionTime = when (preferenceRepository.get<NoteDeletionTime>().first()) {
            NoteDeletionTime.INSTANTLY -> 0L
            NoteDeletionTime.WEEK -> TimeUnit.DAYS.toSeconds(7)
            NoteDeletionTime.TWO_WEEKS -> TimeUnit.DAYS.toSeconds(14)
            NoteDeletionTime.MONTH -> TimeUnit.DAYS.toSeconds(30)
        }

        val now = Instant.now()
        val toBeDeleted = noteRepository.getDeleted(SortMethod.default()).first()
            .filter { note ->
                val deletionDate = note.deletionDate?.let { Instant.ofEpochSecond(it) } ?: return@filter false
                now.isAfter(deletionDate.plusSeconds(deletionTime))
            }
            .toTypedArray()

        noteRepository.deleteNotes(*toBeDeleted)
        storageCleaner.clean()

        Result.success()
    }
}
