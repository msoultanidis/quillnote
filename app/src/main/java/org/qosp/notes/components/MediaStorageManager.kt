package org.qosp.notes.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.qosp.notes.App
import org.qosp.notes.BuildConfig
import org.qosp.notes.data.repo.NoteRepository
import org.qosp.notes.preferences.SortMethod
import org.qosp.notes.ui.attachments.getAttachmentUri
import java.io.File

class MediaStorageManager(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val mediaFolder: String = App.MEDIA_FOLDER,
) {
    suspend fun cleanUpStorage(): Result<Unit, Throwable> = runCatching {
        val filesUsed = noteRepository
            .getAll(SortMethod.default())
            .first()
            .flatMap { it.attachments }
            .map { it.path }

        val dir = File(context.filesDir, mediaFolder)
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

    /***
     * Creates a media file in local storage.
     *
     * @return The file's [Uri] and [File] objects.
     */
    suspend fun createMediaFile(type: MediaType, extension: String = type.defaultExtension): Pair<Uri, File>? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(context.filesDir, App.MEDIA_FOLDER).also { it.mkdirs() }
                val prefix = when (type) {
                    MediaType.IMAGE -> "img_"
                    MediaType.AUDIO -> "audio_"
                }

                val file = File.createTempFile(prefix, extension, dir)
                FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file) to file
            }.get()
        }
    }

    enum class MediaType(val defaultExtension: String){
        IMAGE(".jpg"), AUDIO(".mp3");
    }
}
