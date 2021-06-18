package org.qosp.notes.ui.tags.recycler

interface TagsRecyclerListener {
    fun onItemClick(position: Int)
    fun onLongClick(position: Int): Boolean
    fun checkTagOnClick(): Boolean
}
