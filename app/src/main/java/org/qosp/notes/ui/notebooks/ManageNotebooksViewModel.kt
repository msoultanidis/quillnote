package org.qosp.notes.ui.notebooks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.qosp.notes.data.model.Notebook
import org.qosp.notes.data.repo.NotebookRepository
import javax.inject.Inject

@HiltViewModel
class ManageNotebooksViewModel @Inject constructor(private val notebookRepository: NotebookRepository) : ViewModel() {
    fun deleteNotebooks(vararg notebooks: Notebook) {
        viewModelScope.launch(Dispatchers.IO) {
            notebookRepository.delete(*notebooks)
        }
    }
}
