package com.srvraj311.ui.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

class CodeColors(val plain: Color, val comment: Color, val string: Color, val number: Color, val keyword: Color, val type: Color)

private val keywords = (
    "abstract as async await boolean break byte case catch char class const continue data default delete do double else enum " +
        "export extends false final finally float for from fun function if implements import in instanceof int interface internal " +
        "is let long new null object of open override package private protected public return sealed select set static string " +
        "struct super switch this throw throws true try type typeof undefined val var void when while with yield def elif lambda " +
        "pass print self None True False and or not echo fi then done local readonly where join insert update create table values " +
        "order by group limit into primary key foreign references begin end go defer chan range map nil"
    ).split(' ').toSet()

private val hashComment = setOf("py", "python", "sh", "bash", "shell", "zsh", "yaml", "yml", "rb", "ruby", "toml", "properties")

/** A small, language-agnostic highlighter: comments, strings, numbers, keywords and Capitalised types. */
fun highlight(code: String, lang: String, c: CodeColors): AnnotatedString {
    val l = lang.lowercase().trim()
    val comment = buildString {
        append("/\\*[\\s\\S]*?\\*/|<!--[\\s\\S]*?-->|//[^\\n]*")
        if (l in hashComment) append("|#[^\\n]*")
        if (l == "sql") append("|--[^\\n]*")
    }
    val token = Regex("($comment)|(\"(?:\\\\.|[^\"\\\\\\n])*\"|'(?:\\\\.|[^'\\\\\\n])*'|`(?:\\\\.|[^`\\\\])*`)|(\\b\\d+(?:\\.\\d+)?\\b)|(\\b[A-Za-z_][A-Za-z_0-9]*\\b)")
    return AnnotatedString.Builder(code).apply {
        addStyle(SpanStyle(color = c.plain), 0, code.length)
        for (m in token.findAll(code)) {
            val style = when {
                m.groups[1] != null -> SpanStyle(color = c.comment, fontStyle = FontStyle.Italic)
                m.groups[2] != null -> SpanStyle(color = c.string)
                m.groups[3] != null -> SpanStyle(color = c.number)
                else -> {
                    val w = m.value
                    when {
                        w.lowercase() in keywords && (w in keywords || l == "sql") -> SpanStyle(color = c.keyword, fontWeight = FontWeight.Medium)
                        w[0].isUpperCase() && w.length > 1 -> SpanStyle(color = c.type)
                        else -> null
                    }
                }
            }
            if (style != null) addStyle(style, m.range.first, m.range.last + 1)
        }
    }.toAnnotatedString()
}
