package org.qosp.notes.ui.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.qosp.notes.data.model.Tag
import org.qosp.notes.data.repo.TagRepository
import javax.inject.Inject

data class TagData(val tag: Tag, val inNote: Boolean)

@HiltViewModel
class TagsViewModel @Inject constructor(private val tagRepository: TagRepository) : ViewModel() {

    fun getData(noteId: Long? = null): Flow<List<TagData>> {
        return when (noteId) {
            null -> tagRepository.getAll().map { tags ->
                tags.map { TagData(it, false) }
            }
            else -> tagRepository.getByNoteId(noteId).flatMapLatest { noteTags ->
                tagRepository.getAll().map { tags ->
                    tags.map { TagData(it, it in noteTags) }
                }
            }
        }
    }

    suspend fun insert(tag: Tag): Long {
        return withContext(Dispatchers.IO) {
            tagRepository.insert(tag)
        }
    }

    fun delete(vararg tags: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            tagRepository.delete(*tags)
        }
    }

    fun addTagToNote(tagId: Long, noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            tagRepository.addTagToNote(tagId, noteId)
        }
    }

    fun deleteTagFromNote(tagId: Long, noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            tagRepository.deleteTagFromNote(tagId, noteId)
        }
    }
}
