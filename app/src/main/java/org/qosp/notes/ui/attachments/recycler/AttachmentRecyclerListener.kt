package org.qosp.notes.ui.attachments.recycler

import org.qosp.notes.databinding.LayoutAttachmentBinding

interface AttachmentRecyclerListener {
    fun onItemClick(position: Int, viewBinding: LayoutAttachmentBinding)
    fun onLongClick(position: Int, viewBinding: LayoutAttachmentBinding): Boolean
}
