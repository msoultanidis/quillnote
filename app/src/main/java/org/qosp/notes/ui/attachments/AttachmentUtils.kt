package org.qosp.notes.ui.attachments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.github.michaelbull.result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.qosp.notes.BuildConfig
import org.qosp.notes.data.model.Attachment
import java.io.File

fun getAttachmentFilename(context: Context, uri: Uri): String? {
    return context.contentResolver
        .query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
}

fun Attachment.uri(context: Context) = getAttachmentUri(context, path)

fun Attachment.Companion.fromUri(context: Context, uri: Uri): Attachment {
    val mimeType = context.contentResolver.getType(uri)
    val description = getAttachmentFilename(context, uri) ?: ""
    val type = if (mimeType == null) Attachment.Type.GENERIC else when {
        mimeType.startsWith("image") -> Attachment.Type.IMAGE
        mimeType.startsWith("video") -> Attachment.Type.VIDEO
        mimeType.startsWith("audio") -> Attachment.Type.AUDIO
        else -> Attachment.Type.GENERIC
    }
    return Attachment(type, uri.toString(), description)
}

fun getAttachmentUri(context: Context, path: String): Uri? {
    return when {
        path.startsWith("content://") -> Uri.parse(path)
        path.startsWith("file://") -> Uri.parse(path)
        else -> {
            runCatching { File(context.filesDir, "media").also { it.mkdir() } }
                .flatMap { dir ->
                    runCatching {
                        FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", File(dir, path))
                    }
                }
                .get()
        }
    }
}

fun getAlbumArtBitmap(context: Context, uri: Uri): Result<Bitmap, Throwable> {
    val retriever = MediaMetadataRetriever()
    val result = runCatching {
        retriever
            .run {
                setDataSource(context, uri)
                embeddedPicture
            }
            ?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
    }.flatMap {
        it.toResultOr { Throwable("Could not fetch image.") }
    }

    retriever.release()

    return result
}

suspend fun createPhotoUri(context: Context): Uri? = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "media").also { it.mkdirs() }
    val file = File.createTempFile("img_", ".jpg", dir)
    FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
}

suspend fun createSoundFile(context: Context, extension: String) = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "media").also { it.mkdirs() }
    val file = File.createTempFile("audio_", ".$extension", dir)
    FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file) to file
}
