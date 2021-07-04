package org.qosp.notes.ui.utils.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.getSystemService

class ExtendedEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val textWatchers: MutableList<TextWatcher> = mutableListOf()

    private val textBeforeSelection get() = text?.substring(0 until selectionStart).orEmpty()
    val currentLineStartPos get() = textBeforeSelection.lastIndexOf("\n") + 1
    val currentLineIndex get() = textBeforeSelection.filter { it == '\n' }.length

    val selectedText get() = text?.substring(selectionStart, selectionEnd)

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return super.requestFocus(direction, previouslyFocusedRect).also { tookFocus ->
            if (tookFocus && text != null) setSelection(length())
        }
    }

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
     * Sets the EditText's text without notifying any TextWatchers.
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
