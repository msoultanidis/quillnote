package org.qosp.notes.data.sync.nextcloud

import kotlinx.coroutines.flow.first
import org.qosp.notes.data.model.IdMapping
import org.qosp.notes.data.model.Note
import org.qosp.notes.data.model.Notebook
import org.qosp.notes.data.repo.NotebookRepository
import org.qosp.notes.data.sync.core.*
import org.qosp.notes.data.sync.nextcloud.model.NextcloudNote

class NextcloudManager(
    private val nextcloudAPI: NextcloudAPI,
    private val notebookRepository: NotebookRepository,
) : SyncProvider {

    override suspend fun createNote(
        note: Note,
        config: ProviderConfig
    ): Response<RemoteNote> {
        if (config !is NextcloudConfig) return InvalidConfig()

        val nextcloudNote = note.toRemoteNote()
        if (nextcloudNote.id != 0L) return GenericError("Cannot create note that already exists")

        return tryCalling {
            nextcloudAPI.createNote(nextcloudNote, config)
        }
    }

    override suspend fun deleteNote(
        note: Note,
        config: ProviderConfig,
        mapping: IdMapping,
    ): Response<RemoteNote> {
        if (config !is NextcloudConfig) return InvalidConfig()

        val nextcloudNote = note.toRemoteNote(old = NextcloudNote(mapping.remoteNoteId))

        if (nextcloudNote.id == 0L) return GenericError("Cannot delete note that does not exist.")

        return tryCalling {
            nextcloudAPI.deleteNote(nextcloudNote, config)
            nextcloudNote
        }
    }

    override suspend fun deleteByRemoteId(config: ProviderConfig, vararg remoteIds: Long): Response<Any> {
        if (config !is NextcloudConfig) return InvalidConfig()
        return tryCalling {
            remoteIds.forEach {
                nextcloudAPI.deleteNote(NextcloudNote(id = it), config)
            }
        }
    }

    override suspend fun getAll(config: ProviderConfig): Response<List<RemoteNote>> {
        if (config !is NextcloudConfig) return InvalidConfig()

        return tryCalling {
            nextcloudAPI.getNotes(config)
        }
    }

    override suspend fun updateNote(
        note: Note,
        config: ProviderConfig,
        mapping: IdMapping,
    ): Response<RemoteNote> {
        if (config !is NextcloudConfig) return InvalidConfig()

        val nextcloudNote = note.toRemoteNote(old = NextcloudNote(mapping.remoteNoteId))

        if (nextcloudNote.id == 0L) return GenericError("Cannot update note that does not exist.")

        return tryCalling {
            nextcloudAPI.updateNote(
                nextcloudNote,
                mapping.extras.toString(),
                config,
            )
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
        if (config !is NextcloudConfig) return InvalidConfig()

        return tryCalling {
            nextcloudAPI.testCredentials(config)
        }
    }

    override suspend fun isServerCompatible(config: ProviderConfig): Response<Any> {
        if (config !is NextcloudConfig) return InvalidConfig()

        return tryCalling {
            val capabilities = nextcloudAPI.getNotesCapabilities(config)!!
            val maxServerVersion = capabilities.apiVersion.last().toFloat()

            if (MIN_SUPPORTED_VERSION.toFloat() > maxServerVersion) throw ServerNotSupportedException
        }
    }

    override suspend fun RemoteNote.toLocalNote(old: Note?): Note {
        if (this !is NextcloudNote) return Note()

        return (old ?: Note()).copy(
            title = title,
            content = content,
            notebookId = getNotebookIdForCategory(category),
            isPinned = favorite,
            modifiedDate = modified,
        )
    }

    override suspend fun Note.toRemoteNote(old: RemoteNote?): NextcloudNote {
        return NextcloudNote(
            id = old?.id ?: 0L,
            title = title,
            content = if (isList) taskListToString() else content,
            category = getCategoryFromNotebookId(notebookId),
            favorite = isPinned,
            modified = modifiedDate,
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

    companion object {
        const val MIN_SUPPORTED_VERSION = 1
    }
}
