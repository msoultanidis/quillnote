package org.qosp.notes.ui.editor.markdown

import android.text.Editable
import android.text.Spanned
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.CodeSpan
import io.noties.markwon.editor.EditHandler
import io.noties.markwon.editor.MarkwonEditorUtils
import io.noties.markwon.editor.PersistedSpans

class CodeHandler : EditHandler<CodeSpan> {
    private lateinit var theme: MarkwonTheme

    override fun init(markwon: Markwon) {
        theme = markwon.configuration().theme()
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(CodeSpan::class.java) { CodeSpan(theme) }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: CodeSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        val match = MarkwonEditorUtils.findDelimited(input, spanStart, "`")
        if (match != null) {
            editable.setSpan(
                persistedSpans.get(markdownSpanType()),
                match.start(),
                match.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun markdownSpanType(): Class<CodeSpan> {
        return CodeSpan::class.java
    }
}
