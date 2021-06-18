package org.qosp.notes.ui.common.recycler

import org.qosp.notes.databinding.LayoutNoteBinding

interface NoteRecyclerListener {
    fun onItemClick(position: Int, viewBinding: LayoutNoteBinding)
    fun onLongClick(position: Int, viewBinding: LayoutNoteBinding): Boolean
}
