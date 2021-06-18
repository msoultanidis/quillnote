package org.qosp.notes.components

import android.content.Context
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.flow.first
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.preferences.SortMethod
import org.qosp.notes.ui.attachments.getAttachmentUri
import java.io.File

class StorageCleaner(
    private val context: Context,
    private val noteRepository: NoteRepository,
) {
    suspend fun clean(): Result<Unit, Throwable> = runCatching {
        val filesUsed = noteRepository
            .getAll(SortMethod.default())
            .first()
            .flatMap { it.attachments }
            .map { it.path }

        val dir = File(context.filesDir, "media")
            .also { it.mkdir() }

        val files = dir
            .list()
            .orEmpty()

        for (file in files) {
            if (getAttachmentUri(context, file).toString() !in filesUsed) {
                File(dir, file).delete()
            }
        }
    }
}
