package org.qosp.notes.data.sync.local

import kotlinx.coroutines.flow.first
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.NoteColor
import org.qosp.notes.data.model.Notebook
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.core.InvalidConfig
import org.qosp.notes.data.sync.core.OperationNotSupported
import org.qosp.notes.data.sync.core.ProviderConfig
import org.qosp.notes.data.sync.core.RemoteNote
import org.qosp.notes.data.sync.core.Response
import org.qosp.notes.data.sync.core.Success
import org.qosp.notes.data.sync.core.SyncProvider
import org.qosp.notes.data.sync.local.model.LocalNote
import org.qosp.notes.data.sync.local.model.LocalNoteMetadata
import org.qosp.notes.data.sync.local.model.extractMetadata
import java.time.Instant

class LocalSyncProvider(
    private val localFilesAPI: LocalFilesAPI,
    private val notebookRepository: NotebookRepository,
): SyncProvider {

    private inline fun <T> withConfig(config: ProviderConfig, block: LocalProviderConfig.() -> Response<T>): Response<T> {
        if (config !is LocalProviderConfig) return InvalidConfig()
        return block(config)
    }

    override suspend fun getAll(config: ProviderConfig): Response<List<RemoteNote>> {
        return withConfig(config) {
            Response.from {
                localFilesAPI.getAll(this)
            }
        }
    }

    override suspend fun createNote(note: Note, config: ProviderConfig): Response<RemoteNote> {
        return withConfig(config) {
            Response.from {
                note.toRemoteNote().let { note ->
                    val uri = localFilesAPI.createNoteFile(note, this).uri
                    note.copy(extras = uri.toString())
                }
            }
        }
    }

    override suspend fun deleteNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote> {
        return withConfig(config) {
            Response.from {
                note.toRemoteNote(old = LocalNote(extras = mapping.extras, metadata = LocalNoteMetadata(id = mapping.remoteNoteId))).also {
                    localFilesAPI.deleteNoteFile(it)
                }
            }
        }
    }

    override suspend fun deleteByRemoteId(config: ProviderConfig, vararg remoteIds: Long): Response<Any> {
        return withConfig(config) {
            Response.from {
                localFilesAPI.getAllNoteFiles(this)
                    .filter { (localFilesAPI.getNoteFromFile(it, this)?.id ?: return@filter false) in remoteIds }
                    .forEach { it.delete() }
            }
        }
    }

    override suspend fun updateNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote> {
        return withConfig(config) {
            Response.from {
                localFilesAPI.updateNote(note.toRemoteNote(old = LocalNote(extras = mapping.extras, metadata = LocalNoteMetadata(id = mapping.remoteNoteId))), mapping, this)
            }
        }
    }

    override suspend fun assignIdToNote(remoteNote: RemoteNote, id: Long, config: ProviderConfig): Response<Any> {
        if (remoteNote !is LocalNote) return InvalidConfig()

        return withConfig(config) {
            Response.from {
                localFilesAPI.appendMetadataToFile(
                    remoteNote,
                    LocalNoteMetadata(id = id),
                )
            }
        }
    }

    override suspend fun supportsBin(): Boolean = false

    override suspend fun moveNoteToBin(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote> {
        return OperationNotSupported()
    }

    override suspend fun restoreNote(note: Note, config: ProviderConfig, mapping: IdMapping): Response<RemoteNote> {
        return OperationNotSupported()
    }

    override suspend fun authenticate(config: ProviderConfig): Response<Any> {
        return OperationNotSupported()
    }

    override suspend fun isServerCompatible(config: ProviderConfig): Response<Any> {
        return OperationNotSupported()
    }

    override suspend fun RemoteNote.toLocalNote(old: Note?): Note {
        if (this !is LocalNote) return Note()

        return (old ?: Note()).let {
            it.copy(
                id = id,
                title = title,
                content = content,
                notebookId = getNotebookIdForCategory(notebookName),
                modifiedDate = dateModified,
                isPinned = metadata?.isPinned ?: it.isPinned,
                isHidden = metadata?.isHidden ?: it.isHidden,
                isDeleted = metadata?.isDeleted ?: it.isDeleted,
                isArchived = metadata?.isArchived ?: it.isArchived,
                isMarkdownEnabled = metadata?.isMarkdownEnabled ?: it.isMarkdownEnabled,
                isList = metadata?.isList ?: it.isList,
                taskList = metadata?.taskList ?: it.taskList,
                reminders = metadata?.reminders ?: it.reminders,
                tags = metadata?.tags ?: it.tags,
                creationDate = metadata?.creationDate ?: it.creationDate,
                attachments =  metadata?.attachments ?: it.attachments,
                color = metadata?.color ?: it.color,
                deletionDate = metadata?.deletionDate ?: it.deletionDate,
            )
        }
    }

    override suspend fun Note.toRemoteNote(old: RemoteNote?): LocalNote {
        return LocalNote(
            title = title,
            content = content,
            notebookName = getCategoryFromNotebookId(notebookId),
            dateModified = modifiedDate,
            metadata = extractMetadata(),
            extras = old?.extras,
        )
    }


    private suspend fun getNotebookIdForCategory(category: String): Long? {
        return category
            .ifEmpty { null }
            ?.let {
                notebookRepository.getByName(it).first()?.id ?: notebookRepository.insert(Notebook(name = category))
            }
    }

    private suspend fun getCategoryFromNotebookId(notebookId: Long?): String {
        return notebookId?.let { notebookRepository.getById(it).first()?.name }.orEmpty()
    }

}