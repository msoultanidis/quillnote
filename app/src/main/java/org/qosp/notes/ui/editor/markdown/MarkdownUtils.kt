package org.qosp.notes.ui.editor.markdown

import android.widget.EditText

enum class MarkdownSpan(val value: String) {
    BOLD("**"),
    ITALICS("_"),
    STRIKETHROUGH("~~"),
    CODE("`"),
    QUOTE(">"),
    HEADING("#"),
}

fun EditText.insertMarkdown(markdownSpan: MarkdownSpan) {
    val start = selectionStart
    val end = selectionEnd
    if (start < 0) return

    val textBefore = text.substring(0 until start)
    val lineStart = textBefore.lastIndexOf("\n") + 1
    val currentLine = textBefore.filter { it == '\n' }.length
    var line = text.lines()[currentLine]
    val oldLength = line.length
    val s = markdownSpan.value

    when (markdownSpan) {
        MarkdownSpan.HEADING -> {
            line = when {
                line.matches(Regex("""($s)\1{5,} [^$s]*""")) -> {
                    line.replaceFirst(Regex("""($s)\1{5,} """), "")
                }
                line.matches(Regex("$s+ [^$s]*")) -> "$s$line"
                else -> "$s $line"
            }

            text.replace(lineStart, lineStart + oldLength, line)
            setSelection(lineStart + line.length)
        }
        MarkdownSpan.QUOTE -> {
            line = when {
                line.matches(Regex("$s .*")) -> "$s$line"
                else -> "$s $line"
            }

            text.replace(lineStart, lineStart + oldLength, line)
            setSelection(lineStart + line.length)
        }
        else -> {
            text.insert(start, s)
            text.insert(end + s.length, s)
            setSelection(start + s.length)
        }
    }
}
