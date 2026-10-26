package com.srvraj311.ui.editor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Looks3
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.LooksTwo
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.ui.graphics.vector.ImageVector

enum class SlashAction { NoteLink, NoteCard, Image, Date }

data class SlashItem(
    val id: String,
    val label: String,
    val hint: String,
    val icon: ImageVector,
    val keywords: String,
    /** Markdown to insert; a CARET marker positions the cursor. */
    val md: String? = null,
    val block: Boolean = false,
    val action: SlashAction? = null,
)

val SLASH_ITEMS = listOf(
    SlashItem("h1", "Heading 1", "Big section title", Icons.Filled.LooksOne, "h1 title heading large", "# $CARET", true),
    SlashItem("h2", "Heading 2", "Medium section title", Icons.Filled.LooksTwo, "h2 heading subtitle", "## $CARET", true),
    SlashItem("h3", "Heading 3", "Small section title", Icons.Filled.Looks3, "h3 heading small", "### $CARET", true),
    SlashItem("ul", "Bulleted list", "A simple list", Icons.Filled.FormatListBulleted, "bullet unordered list ul", "- $CARET", true),
    SlashItem("ol", "Numbered list", "An ordered list", Icons.Filled.FormatListNumbered, "number ordered list ol", "1. $CARET", true),
    SlashItem("task", "Checklist", "Track tasks with checkboxes", Icons.Filled.CheckBox, "todo task check checklist", "- [ ] $CARET", true),
    SlashItem("quote", "Quote", "Quote a passage", Icons.Filled.FormatQuote, "quote blockquote cite", "> $CARET", true),
    SlashItem("callout", "Callout", "Highlighted note box", Icons.Filled.Info, "callout note tip warning info alert", "> [!NOTE]\n> $CARET", true),
    SlashItem("code", "Code block", "Syntax-highlighted code", Icons.Filled.Code, "code snippet block fence", "```js\n$CARET\n```\n", true),
    SlashItem("table", "Table", "Rows and columns", Icons.Filled.TableChart, "table grid columns", "| ${CARET}Column | Column |\n| --- | --- |\n|  |  |\n", true),
    SlashItem("hr", "Divider", "Horizontal rule", Icons.Filled.HorizontalRule, "divider line rule separator hr", "---\n$CARET", true),
    SlashItem("notelink", "Link to note", "Insert a link to another note", Icons.Filled.Link, "link note reference mention backlink", action = SlashAction.NoteLink),
    SlashItem("notecard", "Note card", "Embed a preview card of another note", Icons.Filled.NoteAlt, "link note card preview embed", action = SlashAction.NoteCard),
    SlashItem("image", "Image", "Add a photo from your gallery", Icons.Filled.AddPhotoAlternate, "image photo picture gallery upload", action = SlashAction.Image),
    SlashItem("weblink", "Web link", "Link to a website", Icons.Filled.Description, "link url web http", "[${CARET}link text](https://)"),
    SlashItem("mark", "Highlight", "Highlight important text", Icons.Filled.Highlight, "highlight mark emphasis", "==${CARET}text=="),
    SlashItem("date", "Today's date", "Insert the current date", Icons.Filled.Today, "date today time now", action = SlashAction.Date),
)

fun matchSlash(query: String): List<SlashItem> {
    val q = query.lowercase()
    return SLASH_ITEMS.filter { q.isEmpty() || it.label.lowercase().contains(q) || it.keywords.contains(q) || it.id.startsWith(q) }
}
