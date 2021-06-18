package org.qosp.notes.ui.editor.markdown

import android.text.Editable
import android.text.Spanned
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.core.spans.BlockQuoteSpan
import io.noties.markwon.editor.EditHandler
import io.noties.markwon.editor.PersistedSpans

class BlockQuoteHandler : EditHandler<BlockQuoteSpan> {
    private lateinit var theme: MarkwonTheme

    override fun init(markwon: Markwon) {
        theme = markwon.configuration().theme()
    }

    override fun configurePersistedSpans(builder: PersistedSpans.Builder) {
        builder.persistSpan(BlockQuoteSpan::class.java) {
            BlockQuoteSpan(theme)
        }
    }

    override fun handleMarkdownSpan(
        persistedSpans: PersistedSpans,
        editable: Editable,
        input: String,
        span: BlockQuoteSpan,
        spanStart: Int,
        spanTextLength: Int
    ) {
        editable.setSpan(
            persistedSpans.get(markdownSpanType()),
            spanStart,
            spanStart + spanTextLength,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    override fun markdownSpanType(): Class<BlockQuoteSpan> {
        return BlockQuoteSpan::class.java
    }
}
