package org.qosp.notes.ui.utils.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService
import io.noties.markwon.editor.MarkwonEditorTextWatcher

class ExtendedEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val textWatchers: MutableList<TextWatcher> = mutableListOf()

    // With a regular EditText, users can paste rich text inside which may look out of place.
    // This function prevents that from happening by changing the clip board
    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return super.onTextContextMenuItem(android.R.id.pasteAsPlainText)
            }

            // If device doesn't support paste as plain text
            val manager = context.getSystemService<ClipboardManager>() ?: return super.onTextContextMenuItem(id)
            val clipData = manager.primaryClip ?: return super.onTextContextMenuItem(id)

            for (i in 0 until clipData.itemCount) {
                val text = clipData.getItemAt(i).coerceToText(context).toString()
                manager.setPrimaryClip(ClipData.newPlainText("", text))
            }
        }

        return super.onTextContextMenuItem(id)
    }

    override fun addTextChangedListener(watcher: TextWatcher?) {
        if (watcher != null) textWatchers.add(watcher)
        super.addTextChangedListener(watcher)
    }

    override fun removeTextChangedListener(watcher: TextWatcher?) {
        if (watcher != null) textWatchers.remove(watcher)
        super.removeTextChangedListener(watcher)
    }

    /**
     * Set's the EditText's text without notifying any TextWatchers.
     *
     * @param text Text to set
     */
    fun setTextSilently(text: CharSequence?) {
        val watchers = textWatchers.toList()
        watchers.forEach { removeTextChangedListener(it) }

        setText(text)

        watchers.forEach { addTextChangedListener(it) }
    }
}

/**
 * Set's the EditText's text without notifying any TextWatchers which are not [MarkwonEditorTextWatcher].
 *
 * @param text Text to set
 */
fun ExtendedEditText.setMarkdownTextSilently(text: CharSequence?) {
    val watchers = textWatchers
        .filterNot { it is MarkwonEditorTextWatcher }
        .toList()

    watchers.forEach { removeTextChangedListener(it) }

    setText(text)

    watchers.forEach { addTextChangedListener(it) }
}
