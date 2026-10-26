package com.srvraj311.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

class LiveColors(val text: Color, val dim: Color, val accent: Color, val codeBg: Color, val markBg: Color)

private val headingRe = Regex("^(#{1,6})(\\s+)(.*)$")
private val quoteRe = Regex("^(\\s*>\\s?)(.*)$")
private val listRe = Regex("^(\\s*)([-*+]|\\d+\\.)(\\s+)(\\[[ xX]]\\s+)?")
private val boldRe = Regex("\\*\\*(.+?)\\*\\*")
private val italicRe = Regex("(?<![*\\w])\\*(?!\\*)(.+?)(?<!\\*)\\*(?![*\\w])")
private val strikeRe = Regex("~~(.+?)~~")
private val codeRe = Regex("`([^`\\n]+)`")
private val markRe = Regex("==(.+?)==")
private val imageRe = Regex("!\\[[^\\]]*]\\([^)]*\\)")
private val linkRe = Regex("(?<!!)\\[([^\\]]+)]\\(([^)]*)\\)")

private val headingSizes = listOf(26.sp, 23.sp, 20.sp, 18.sp, 17.sp, 16.sp)

/** Styles the raw markdown in place (same characters, so the cursor mapping is the identity). */
fun liveStyled(text: String, c: LiveColors): AnnotatedString = AnnotatedString.Builder(text).apply {
    var offset = 0
    var inFence = false
    for (line in text.split('\n')) {
        val end = offset + line.length
        fun range(from: Int, to: Int) = (offset + from) to (offset + to)

        if (line.trimStart().startsWith("```")) {
            inFence = !inFence
            addStyle(SpanStyle(color = c.dim, fontFamily = FontFamily.Monospace, background = c.codeBg), offset, end)
        } else if (inFence) {
            addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = c.codeBg, fontSize = 14.sp), offset, end)
        } else {
            val h = headingRe.find(line)
            val q = quoteRe.find(line)
            val l = listRe.find(line)
            when {
                h != null -> {
                    val level = h.groupValues[1].length
                    addStyle(SpanStyle(fontSize = headingSizes[level - 1], fontWeight = FontWeight.Bold), offset, end)
                    addStyle(SpanStyle(color = c.dim, fontWeight = FontWeight.Normal), offset, offset + level + h.groupValues[2].length)
                }
                q != null -> {
                    addStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.Bold), offset, offset + q.groupValues[1].length)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic, color = c.dim.copy(alpha = 1f)), offset + q.groupValues[1].length, end)
                }
                l != null -> {
                    val (a, b) = range(l.groupValues[1].length, l.groupValues[1].length + l.groupValues[2].length)
                    addStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.Bold), a, b)
                    val task = l.groupValues[4]
                    if (task.isNotEmpty()) {
                        val ts = offset + l.range.last + 1 - task.length
                        addStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.Bold), ts, ts + 3)
                        if (task.contains('x', ignoreCase = true)) addStyle(SpanStyle(color = c.dim, textDecoration = TextDecoration.LineThrough), offset + l.range.last + 1, end)
                    }
                }
            }
            val dimStyle = SpanStyle(color = c.dim)
            boldRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), offset + m.range.first, offset + m.range.last + 1)
                addStyle(dimStyle, offset + m.range.first, offset + m.range.first + 2); addStyle(dimStyle, offset + m.range.last - 1, offset + m.range.last + 1)
            }
            italicRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), offset + m.range.first, offset + m.range.last + 1)
                addStyle(dimStyle, offset + m.range.first, offset + m.range.first + 1); addStyle(dimStyle, offset + m.range.last, offset + m.range.last + 1)
            }
            strikeRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), offset + m.range.first, offset + m.range.last + 1)
                addStyle(dimStyle, offset + m.range.first, offset + m.range.first + 2); addStyle(dimStyle, offset + m.range.last - 1, offset + m.range.last + 1)
            }
            codeRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = c.codeBg, fontSize = 15.sp), offset + m.range.first, offset + m.range.last + 1)
            }
            markRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(background = c.markBg), offset + m.range.first, offset + m.range.last + 1)
                addStyle(dimStyle, offset + m.range.first, offset + m.range.first + 2); addStyle(dimStyle, offset + m.range.last - 1, offset + m.range.last + 1)
            }
            imageRe.findAll(line).forEach { m ->
                addStyle(SpanStyle(color = c.accent, background = c.codeBg), offset + m.range.first, offset + m.range.last + 1)
            }
            linkRe.findAll(line).forEach { m ->
                val textEnd = offset + m.range.first + 1 + m.groupValues[1].length
                addStyle(SpanStyle(color = c.accent, fontWeight = FontWeight.Medium), offset + m.range.first + 1, textEnd)
                addStyle(dimStyle, offset + m.range.first, offset + m.range.first + 1)
                addStyle(dimStyle, textEnd, offset + m.range.last + 1)
            }
        }
        offset = end + 1
    }
}.toAnnotatedString()

class LiveTransformation(private val colors: LiveColors) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText = TransformedText(liveStyled(text.text, colors), OffsetMapping.Identity)
}
