package org.qosp.notes.ui.editor.markdown

import android.text.Editable
import android.text.Spanned
import android.text.style.StrikethroughSpan
import io.noties.markwon.editor.AbstractEditHandler
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

class StrikethroughHandler : AbstractEditHandler<StrikethroughSpan>() {
    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(markdownSpanType()) { StrikethroughSpan() }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: StrikethroughSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "~~")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(markdownSpanType()),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<StrikethroughSpan> {
        return StrikethroughSpan::class.java
    }
}
