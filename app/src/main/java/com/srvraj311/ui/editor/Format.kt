package com.srvraj311.ui.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Marks where the caret should land after inserting a snippet. */
const val CARET = '‸'

private val blockPatterns = mapOf(
    "h1" to Regex("^# "), "h2" to Regex("^## "), "ul" to Regex("^[-*+] (?!\\[[ xX]] )"),
    "ol" to Regex("^\\d+\\. "), "task" to Regex("^[-*+] \\[[ xX]] "), "quote" to Regex("^> "),
)
private val anyBlock = Regex("^(#{1,6} |[-*+] \\[[ xX]] |[-*+] |\\d+\\. |> )")
private val listLine = Regex("^(\\s*)([-*+] \\[[ xX]] |[-*+] |\\d+\\. |> )(.*)$")

/** Markdown editing operations on a text field value (text + selection). */
object Fmt {
    /** **bold**, *italic*, ~~strike~~, `code`: toggles when already wrapped. */
    fun wrap(v: TextFieldValue, m: String): TextFieldValue {
        val t = v.text
        val s = v.selection.min
        val e = v.selection.max
        val sel = t.substring(s, e)
        val wrapped = s >= m.length && e + m.length <= t.length && t.substring(s - m.length, s) == m && t.substring(e, e + m.length) == m
        return when {
            wrapped -> v.copy(text = t.substring(0, s - m.length) + sel + t.substring(e + m.length), selection = TextRange(s - m.length, e - m.length))
            sel.isNotEmpty() -> v.copy(text = t.substring(0, s) + m + sel + m + t.substring(e), selection = TextRange(s + m.length, e + m.length))
            else -> v.copy(text = t.substring(0, s) + m + "text" + m + t.substring(e), selection = TextRange(s + m.length, s + m.length + 4))
        }
    }

    /** Headings, lists, checklist and quote apply to every selected line and toggle off when all lines have them. */
    fun block(v: TextFieldValue, kind: String): TextFieldValue {
        val t = v.text
        val start = t.lastIndexOf('\n', v.selection.min - 1) + 1
        var end = t.indexOf('\n', v.selection.max)
        if (end == -1) end = t.length
        val lines = t.substring(start, end).split('\n')
        val re = blockPatterns.getValue(kind)
        val allOn = lines.all { re.containsMatchIn(it.trimStart()) }
        val out = lines.mapIndexed { i, line ->
            val indent = line.takeWhile { it == ' ' || it == '\t' }
            val body = line.substring(indent.length)
            if (allOn) indent + body.replaceFirst(re, "")
            else {
                val prefix = when (kind) { "h1" -> "# "; "h2" -> "## "; "ul" -> "- "; "ol" -> "${i + 1}. "; "task" -> "- [ ] "; else -> "> " }
                indent + prefix + body.replaceFirst(anyBlock, "")
            }
        }.joinToString("\n")
        return v.copy(text = t.substring(0, start) + out + t.substring(end), selection = TextRange(start, start + out.length))
    }

    fun codeBlock(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val s = v.selection.min
        val e = v.selection.max
        val sel = t.substring(s, e)
        val lead = if (s > 0 && t[s - 1] != '\n') "\n" else ""
        val text = "$lead```\n$sel\n```\n"
        return v.copy(text = t.substring(0, s) + text + t.substring(e), selection = TextRange(s + lead.length + 4, s + lead.length + 4 + sel.length))
    }

    fun link(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val s = v.selection.min
        val e = v.selection.max
        val sel = t.substring(s, e).ifEmpty { "link text" }
        val urlStart = s + sel.length + 3
        return v.copy(text = t.substring(0, s) + "[$sel](https://)" + t.substring(e), selection = TextRange(urlStart, urlStart + 8))
    }

    fun rule(v: TextFieldValue): TextFieldValue {
        val t = v.text
        val s = v.selection.min
        val text = (if (s > 0 && t[s - 1] != '\n') "\n" else "") + "\n---\n\n"
        return v.copy(text = t.substring(0, s) + text + t.substring(v.selection.max), selection = TextRange(s + text.length))
    }

    /** Replaces [start, end) with [snippet]; a CARET marker in the snippet positions the cursor. */
    fun insert(v: TextFieldValue, start: Int, end: Int, snippet: String, block: Boolean = false): TextFieldValue {
        val t = v.text
        val lead = if (block && start > 0 && t[start - 1] != '\n') "\n" else ""
        val i = snippet.indexOf(CARET)
        val clean = snippet.replace(CARET.toString(), "")
        val caret = start + lead.length + if (i == -1) clean.length else i
        return v.copy(text = t.substring(0, start) + lead + clean + t.substring(end), selection = TextRange(caret))
    }

    /**
     * After a newline was typed inside a list or quote, continue it (and end it on an empty item).
     * Returns null when [new] is not a plain "Enter" or the line is not a list line.
     */
    fun continueList(old: TextFieldValue, new: TextFieldValue): TextFieldValue? {
        if (new.text.length != old.text.length + 1 || !new.selection.collapsed) return null
        val pos = new.selection.start
        if (pos < 1 || new.text[pos - 1] != '\n') return null
        if (new.text.removeRange(pos - 1, pos) != old.text) return null
        val lineStart = new.text.lastIndexOf('\n', pos - 2) + 1
        val m = listLine.matchEntire(new.text.substring(lineStart, pos - 1)) ?: return null
        val (indent, marker, rest) = m.destructured
        if (rest.isBlank()) {
            // Enter on an empty item ends the list: remove the marker and the newline.
            return new.copy(text = new.text.substring(0, lineStart) + new.text.substring(pos), selection = TextRange(lineStart))
        }
        var next = marker
        if (Regex("^\\d+\\. $").matches(marker)) next = "${marker.trim().dropLast(1).toInt() + 1}. "
        else if (Regex("\\[[ xX]]").containsMatchIn(marker)) next = marker.replace(Regex("\\[[ xX]]"), "[ ]")
        val add = indent + next
        return new.copy(text = new.text.substring(0, pos) + add + new.text.substring(pos), selection = TextRange(pos + add.length))
    }

    /** The "/word" the caret is right after, as (start index, query), or null. */
    fun slash(v: TextFieldValue): Pair<Int, String>? {
        if (!v.selection.collapsed) return null
        val pos = v.selection.start
        val before = v.text.substring(0, pos)
        val m = Regex("(^|\\s)/([\\w-]{0,24})$").find(before) ?: return null
        return (pos - m.groupValues[2].length - 1) to m.groupValues[2]
    }
}
