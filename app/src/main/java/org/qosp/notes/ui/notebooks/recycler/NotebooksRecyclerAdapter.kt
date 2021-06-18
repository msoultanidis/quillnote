package org.qosp.notes.ui.notebooks.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import org.qosp.notes.data.model.Notebook
import org.qosp.notes.databinding.LayoutNotebookBinding
import org.qosp.notes.ui.common.recycler.ExtendedListAdapter

class NotebooksRecyclerAdapter(
    var listener: NotebooksRecyclerListener?,
) :
    ExtendedListAdapter<Notebook, NotebooksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotebooksViewHolder {
        val binding: LayoutNotebookBinding =
            LayoutNotebookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotebooksViewHolder(parent.context, binding, listener)
    }

    override fun onBindViewHolder(holder: NotebooksViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            return super.onBindViewHolder(holder, position, payloads)
        }
        holder.runPayloads(getItem(position), payloads.map { it as Payload })
    }

    override fun onBindViewHolder(holder: NotebooksViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val notebook: Notebook = getItem(position)
        holder.bind(notebook)
    }

    override fun getItemId(position: Int) = getItem(position).id

    private class DiffCallback : DiffUtil.ItemCallback<Notebook>() {
        override fun areItemsTheSame(oldItem: Notebook, newItem: Notebook): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notebook, newItem: Notebook): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Notebook, newItem: Notebook): Payload? {
            return if (oldItem.name != newItem.name) Payload.NameChanged else null
        }
    }

    enum class Payload {
        NameChanged
    }
}
