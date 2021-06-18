package org.qosp.notes.ui.tags.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.qosp.notes.databinding.LayoutTagBinding
import org.qosp.notes.ui.common.recycler.ExtendedListAdapter
import org.qosp.notes.ui.tags.TagData

class TagsRecyclerAdapter(
    private val noteId: Long? = null,
    var listener: TagsRecyclerListener?,
) :
    ExtendedListAdapter<TagData, TagsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsViewHolder {
        val binding: LayoutTagBinding =
            LayoutTagBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagsViewHolder(parent.context, binding, listener, noteId)
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else {
            holder.runPayloads(getItem(position), payloads.map { it as Payload })
        }
    }

    override fun onBindViewHolder(holder: TagsViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val tag = getItem(position)
        holder.bind(tag)
    }

    override fun getItemId(position: Int) = getItem(position).tag.id

    private class DiffCallback : DiffUtil.ItemCallback<TagData>() {
        override fun areItemsTheSame(oldItem: TagData, newItem: TagData): Boolean {
            return oldItem.tag.id == newItem.tag.id
        }

        override fun areContentsTheSame(oldItem: TagData, newItem: TagData): Boolean {
            return oldItem.tag == newItem.tag
        }

        override fun getChangePayload(oldItem: TagData, newItem: TagData): Payload? {
            return if (oldItem.tag.name != newItem.tag.name) Payload.NameChanged else null
        }
    }

    enum class Payload {
        NameChanged
    }
}
