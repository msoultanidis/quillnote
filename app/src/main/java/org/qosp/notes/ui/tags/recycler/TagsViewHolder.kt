package org.qosp.notes.ui.tags.recycler

import android.content.Context
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.qosp.notes.databinding.LayoutTagBinding
import org.qosp.notes.ui.common.recycler.SelectableViewHolder
import org.qosp.notes.ui.tags.TagData

class TagsViewHolder(
    private val context: Context,
    private val binding: LayoutTagBinding,
    listener: TagsRecyclerListener?,
    private val noteId: Long?,
) : RecyclerView.ViewHolder(binding.root), SelectableViewHolder {

    init {
        if (listener != null) {
            itemView.setOnLongClickListener { listener.onLongClick(bindingAdapterPosition) }

            itemView.setOnClickListener {
                if (listener.checkTagOnClick()) binding.checkBox.isChecked = !binding.checkBox.isChecked
                listener.onItemClick(bindingAdapterPosition)
            }
        }

        binding.checkBox.isClickable = false
    }

    private fun setName(name: String) {
        binding.textViewTagName.text = name
    }

    fun bind(data: TagData) = with(binding) {
        setName(data.tag.name)
        imageView.isVisible = noteId == null
        checkBox.isVisible = noteId != null
        checkBox.isChecked = data.inNote
    }

    fun runPayloads(tagData: TagData, payloads: List<TagsRecyclerAdapter.Payload>) {
        payloads.forEach {
            when (it) {
                TagsRecyclerAdapter.Payload.NameChanged -> setName(tagData.tag.name)
            }
        }
    }

    override fun onSelectedStatusChanged(isSelected: Boolean) {
        binding.root.isSelected = isSelected
    }
}
