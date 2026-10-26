package com.srvraj311.ui.editor

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.commonmark.ext.gfm.strikethrough.Strikethrough
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.BlockQuote
import org.commonmark.node.BulletList
import org.commonmark.node.Code
import org.commonmark.node.Emphasis
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.HardLineBreak
import org.commonmark.node.Heading
import org.commonmark.node.HtmlBlock
import org.commonmark.node.HtmlInline
import org.commonmark.node.Image as MdImage
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Link
import org.commonmark.node.ListItem
import org.commonmark.node.Node
import org.commonmark.node.OrderedList
import org.commonmark.node.Paragraph
import org.commonmark.node.SoftLineBreak
import org.commonmark.node.StrongEmphasis
import org.commonmark.node.Text as MdText
import org.commonmark.node.ThematicBreak
import org.commonmark.parser.Parser

const val NOTE_SCHEME = "enwrite://note/"

/** A reference to something an image viewer can show: base64 data URI or http(s) URL. */
data class ImageRef(val name: String, val source: String)

/** What a note link or card needs to display. */
data class LinkedNote(val id: String, val title: String, val snippet: String, val colour: Color?)

private val parser: Parser = Parser.builder().extensions(listOf(TablesExtension.create(), StrikethroughExtension.create())).build()
private val taskRe = Regex("^\\[([ xX])]\\s?")
private val calloutRe = Regex("^\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\\s*", RegexOption.IGNORE_CASE)
private val markRe = Regex("==(.+?)==")

private class Ctx(
    val images: Map<String, String>,
    val resolveNote: (String) -> LinkedNote?,
    val onToggleTask: (Int) -> Unit,
    val onOpenNote: (String) -> Unit,
    val onImage: (ImageRef) -> Unit,
) {
    var taskIndex = 0
}

/** Read-only rendered view of a markdown note. Tasks, note links, cards and images are interactive. */
@Composable
fun MarkdownView(
    markdown: String,
    images: Map<String, String>,
    resolveNote: (String) -> LinkedNote?,
    onToggleTask: (Int) -> Unit,
    onOpenNote: (String) -> Unit,
    onImage: (ImageRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    val doc = remember(markdown) { parser.parse(markdown) }
    val ctx = Ctx(images, resolveNote, onToggleTask, onOpenNote, onImage)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) { Blocks(doc, ctx) }
}

private fun children(node: Node): List<Node> = generateSequence(node.firstChild) { it.next }.toList()

@Composable
private fun Blocks(parent: Node, ctx: Ctx, stripCallout: Boolean = false) {
    var first = true
    for (n in children(parent)) {
        Block(n, ctx, stripCallout && first)
        first = false
    }
}

@Composable
private fun Block(n: Node, ctx: Ctx, stripCallout: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    when (n) {
        is Heading -> {
            val size = listOf(28.sp, 24.sp, 21.sp, 18.sp, 16.sp, 15.sp)[(n.level - 1).coerceIn(0, 5)]
            Column(Modifier.padding(top = if (n.level <= 2) 8.dp else 4.dp)) {
                Text(inline(n, ctx).first, fontSize = size, fontWeight = FontWeight.Bold, lineHeight = size * 1.25f, color = if (n.level >= 4) colors.onSurfaceVariant else colors.onSurface)
                if (n.level == 2) HorizontalDivider(Modifier.padding(top = 6.dp), color = colors.outline)
            }
        }
        is Paragraph -> ParagraphBlock(n, ctx, stripCallout)
        is BulletList, is OrderedList -> ListBlock(n, ctx)
        is BlockQuote -> QuoteBlock(n, ctx)
        is FencedCodeBlock -> CodeBlock(n.literal.trimEnd('\n'), n.info.orEmpty())
        is IndentedCodeBlock -> CodeBlock(n.literal.trimEnd('\n'), "")
        is ThematicBreak -> HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outline)
        is TableBlock -> TableView(n, ctx)
        is HtmlBlock -> Text(n.literal.trim(), color = colors.onSurfaceVariant, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        else -> Blocks(n, ctx)
    }
}

// ---- inline content ---------------------------------------------------------------------------

private class InlineResult(val first: AnnotatedString, val images: List<ImageRef>, val soloNoteCard: LinkedNote?)

@Composable
private fun inline(parent: Node, ctx: Ctx, stripTask: Boolean = false, stripCallout: Boolean = false): Pair<AnnotatedString, List<ImageRef>> {
    val r = inlineFull(parent, ctx, stripTask, stripCallout)
    return r.first to r.images
}

@Composable
private fun inlineFull(parent: Node, ctx: Ctx, stripTask: Boolean = false, stripCallout: Boolean = false): InlineResult {
    val colors = MaterialTheme.colorScheme
    val codeBg = colors.onSurface.copy(alpha = .09f)
    val markBg = Color(0xFFFFD54A).copy(alpha = .45f)
    val linkStyle = TextLinkStyles(SpanStyle(color = colors.primary, textDecoration = TextDecoration.Underline))
    val chipStyle = TextLinkStyles(SpanStyle(color = colors.primary, background = colors.primary.copy(alpha = .12f), fontWeight = FontWeight.Medium))
    val images = mutableListOf<ImageRef>()
    var solo: LinkedNote? = null

    val text = buildAnnotatedString {
        var skipBreak = false
        var firstText = true

        fun appendWithMarks(s: String) {
            var last = 0
            for (m in markRe.findAll(s)) {
                append(s.substring(last, m.range.first))
                withStyle(SpanStyle(background = markBg)) { append(m.groupValues[1]) }
                last = m.range.last + 1
            }
            append(s.substring(last))
        }

        fun walk(node: Node) {
            for (c in children(node)) {
                when (c) {
                    is MdText -> {
                        var lit = c.literal
                        if (firstText) {
                            firstText = false
                            if (stripTask) lit = lit.replaceFirst(taskRe, "")
                            if (stripCallout) { val m = calloutRe.find(lit); if (m != null) { lit = lit.removeRange(m.range); skipBreak = true } }
                        }
                        appendWithMarks(lit)
                    }
                    is SoftLineBreak, is HardLineBreak -> { if (skipBreak) skipBreak = false else append("\n") }
                    is Emphasis -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { walk(c) }
                    is StrongEmphasis -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { walk(c) }
                    is Strikethrough -> withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { walk(c) }
                    is Code -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg, fontSize = 14.sp)) { append(c.literal) }
                    is Link -> {
                        val dest = c.destination.orEmpty()
                        if (dest.startsWith(NOTE_SCHEME)) {
                            val id = dest.removePrefix(NOTE_SCHEME)
                            val label = children(c).filterIsInstance<MdText>().joinToString("") { it.literal }
                            if (c.title == "card" && children(parent).size == 1) solo = ctx.resolveNote(id) ?: LinkedNote(id, label, "", null)
                            else withLink(LinkAnnotation.Clickable("note:$id", chipStyle) { ctx.onOpenNote(id) }) { append(" $label ↗ ") }
                        } else withLink(LinkAnnotation.Url(dest, linkStyle)) { walk(c) }
                    }
                    is MdImage -> images += ImageRef(children(c).filterIsInstance<MdText>().joinToString("") { it.literal }.ifBlank { "image" }, c.destination.orEmpty())
                    is HtmlInline -> append(c.literal)
                    else -> walk(c)
                }
            }
        }
        walk(parent)
    }
    return InlineResult(text, images, solo)
}

@Composable
private fun ParagraphBlock(p: Paragraph, ctx: Ctx, stripCallout: Boolean) {
    val r = inlineFull(p, ctx, stripCallout = stripCallout)
    val colors = MaterialTheme.colorScheme
    r.soloNoteCard?.let { NoteCardBlock(it, ctx); return }
    if (r.first.text.isNotBlank()) Text(r.first, fontSize = 16.sp, lineHeight = 25.sp, color = colors.onSurface)
    r.images.forEach { ImageBlock(it, ctx) }
}

@Composable
private fun NoteCardBlock(note: LinkedNote, ctx: Ctx) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.widthIn(max = 520.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(colors.surface)
            .clickable { ctx.onOpenNote(note.id) }.height(IntrinsicSize.Min),
    ) {
        Box(Modifier.width(5.dp).fillMaxHeight().background(note.colour ?: colors.primary))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(note.title.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(note.snippet.ifBlank { "No additional text" }, color = colors.onSurfaceVariant, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(12.dp).size(18.dp))
    }
}

@Composable
private fun ImageBlock(ref: ImageRef, ctx: Ctx) {
    val src = if (ref.source.startsWith("img:")) ctx.images[ref.source.removePrefix("img:")].orEmpty() else ref.source
    if (src.isEmpty()) {
        Text("Image not available", color = MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic, fontSize = 14.sp)
        return
    }
    val shape = RoundedCornerShape(14.dp)
    val modifier = Modifier.fillMaxWidth().clip(shape).clickable { ctx.onImage(ImageRef(ref.name, src)) }
    if (src.startsWith("data:")) {
        val bitmap = remember(src) { decodeDataUri(src) }
        if (bitmap != null) Image(bitmap.asImageBitmap(), ref.name, modifier, contentScale = ContentScale.FillWidth)
    } else {
        AsyncImage(model = src, contentDescription = ref.name, modifier = modifier, contentScale = ContentScale.FillWidth)
    }
}

fun decodeDataUri(uri: String): android.graphics.Bitmap? = runCatching {
    val bytes = Base64.decode(uri.substringAfter("base64,"), Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}.getOrNull()

// ---- blocks -----------------------------------------------------------------------------------

@Composable
private fun ListBlock(list: Node, ctx: Ctx) {
    val colors = MaterialTheme.colorScheme
    var number = (list as? OrderedList)?.markerStartNumber ?: 1
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (item in children(list).filterIsInstance<ListItem>()) {
            val kids = children(item)
            val firstPara = kids.firstOrNull() as? Paragraph
            val firstText = firstPara?.firstChild as? MdText
            val task = firstText?.let { taskRe.find(it.literal) }
            val index = if (task != null) ctx.taskIndex++ else -1
            Row(verticalAlignment = Alignment.Top) {
                when {
                    task != null -> {
                        val done = task.groupValues[1].isNotBlank()
                        Checkbox(
                            checked = done, onCheckedChange = { ctx.onToggleTask(index) },
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary),
                            modifier = Modifier.padding(end = 2.dp).size(32.dp),
                        )
                    }
                    list is OrderedList -> Text("${number++}.", color = colors.onSurfaceVariant, modifier = Modifier.width(30.dp).padding(top = 1.dp), fontSize = 16.sp)
                    else -> Text("•", color = colors.primary, modifier = Modifier.width(24.dp).padding(start = 6.dp), fontSize = 18.sp)
                }
                Column(Modifier.weight(1f).padding(top = if (task != null) 4.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((i, k) in kids.withIndex()) {
                        if (i == 0 && k is Paragraph) {
                            val r = inlineFull(k, ctx, stripTask = task != null)
                            val done = task?.groupValues?.get(1)?.isNotBlank() == true
                            Text(
                                r.first, fontSize = 16.sp, lineHeight = 24.sp,
                                color = if (done) colors.onSurfaceVariant else colors.onSurface,
                                textDecoration = if (done) TextDecoration.LineThrough else null,
                            )
                            r.images.forEach { ImageBlock(it, ctx) }
                        } else Block(k, ctx)
                    }
                }
            }
        }
    }
}

private val calloutColours = mapOf(
    "NOTE" to Color(0xFFEF0A6A), "TIP" to Color(0xFF16A34A), "IMPORTANT" to Color(0xFF8B5CF6),
    "WARNING" to Color(0xFFF59E0B), "CAUTION" to Color(0xFFEF4444),
)

@Composable
private fun QuoteBlock(q: BlockQuote, ctx: Ctx) {
    val colors = MaterialTheme.colorScheme
    val firstText = ((q.firstChild as? Paragraph)?.firstChild as? MdText)?.literal.orEmpty()
    val kind = calloutRe.find(firstText)?.groupValues?.get(1)?.uppercase()
    val accent = kind?.let { calloutColours[it] } ?: colors.primary
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = if (kind != null) .12f else .08f)).height(IntrinsicSize.Min)) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(accent))
        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (kind != null) Text(kind.lowercase().replaceFirstChar { it.uppercase() }.uppercase(), color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 0.6.sp)
            Blocks(q, ctx, stripCallout = kind != null)
        }
    }
}

@Composable
private fun CodeBlock(code: String, lang: String) {
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.red < 0.3f
    val palette = if (dark) CodeColors(colors.onSurface, colors.onSurfaceVariant, Color(0xFF4ADE80), Color(0xFFFBBF24), Color(0xFFE879F9), Color(0xFF60A5FA))
    else CodeColors(colors.onSurface, colors.onSurfaceVariant, Color(0xFF16A34A), Color(0xFFD97706), Color(0xFFC026D3), Color(0xFF2563EB))
    val text = remember(code, lang, dark) { highlight(code, lang, palette) }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.onSurface.copy(alpha = .07f))) {
        if (lang.isNotBlank()) Text(lang.lowercase(), color = colors.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(start = 14.dp, top = 8.dp))
        Text(text, fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp, softWrap = false,
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(14.dp), style = TextStyle(fontFamily = FontFamily.Monospace))
    }
}

@Composable
private fun TableView(table: TableBlock, ctx: Ctx) {
    val colors = MaterialTheme.colorScheme
    val rows = buildList {
        for (section in children(table)) for (row in children(section)) add((section is TableHead) to children(row).filterIsInstance<TableCell>())
    }
    Column(Modifier.horizontalScroll(rememberScrollState()).clip(RoundedCornerShape(10.dp)).background(colors.surface)) {
        rows.forEachIndexed { i, (header, cells) ->
            Row(Modifier.background(if (header) colors.onSurface.copy(alpha = .06f) else Color.Transparent).height(IntrinsicSize.Min)) {
                cells.forEach { cell ->
                    val text = inlineFull(cell, ctx).first
                    Text(text, fontWeight = if (header) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp, modifier = Modifier.width(128.dp).padding(horizontal = 12.dp, vertical = 10.dp))
                }
            }
            if (i < rows.lastIndex) HorizontalDivider(color = colors.outline)
        }
    }
}
