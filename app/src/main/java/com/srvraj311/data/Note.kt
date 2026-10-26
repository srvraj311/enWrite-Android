package com.srvraj311.data

import androidx.compose.ui.graphics.Color
import java.util.UUID

const val NO_COLOUR = "#FFFFFF"
val NOTE_COLOURS = listOf("#FFFFFF", "#ACACAC", "#FFF171", "#e090d3", "#2fc9da", "#ff7f7f", "#519F54", "#aa90e0")

/**
 * A note. The Firestore field names (note_id, note_title, ...) are the same ones the web and desktop apps use,
 * so notes sync between every enWrite app.
 */
data class Note(
    val id: String,
    val title: String = "",
    val body: String = "",
    val date: Long = System.currentTimeMillis(),
    val colour: String = NO_COLOUR,
    val pinned: Boolean = false,
    /** Small pasted images kept with the note, referenced from the markdown as `img:<id>`. */
    val images: Map<String, String> = emptyMap(),
) {
    val isBlank get() = title.isBlank() && body.isBlank()
    val hasColour get() = !colour.equals(NO_COLOUR, ignoreCase = true)

    fun toMap(): Map<String, Any> = buildMap {
        put("note_id", id)
        put("note_title", title)
        put("note_body", body)
        put("note_date", date.toString())
        put("note_colour", colour)
        put("pinned", pinned)
        if (images.isNotEmpty()) put("note_images", images)
    }

    companion object {
        fun fromMap(id: String, m: Map<String, Any?>): Note = Note(
            id = id,
            title = m["note_title"] as? String ?: "",
            body = m["note_body"] as? String ?: "",
            date = (m["note_date"] as? String)?.toLongOrNull() ?: (m["note_date"] as? Number)?.toLong() ?: 0L,
            colour = m["note_colour"] as? String ?: NO_COLOUR,
            pinned = m["pinned"] == true || m["pinned"] == "true",
            images = (m["note_images"] as? Map<*, *>)
                ?.mapNotNull { (k, v) -> if (k is String && v is String) k to v else null }?.toMap()
                ?: emptyMap(),
        )
    }
}

fun newNote() = Note(id = UUID.randomUUID().toString())

fun parseColour(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (_: IllegalArgumentException) {
    Color.Transparent
}

/** "Just now", "5m ago", "Yesterday"... */
fun timeAgo(millis: Long): String {
    if (millis <= 0) return ""
    val s = ((System.currentTimeMillis() - millis) / 1000).coerceAtLeast(0)
    return when {
        s < 45 -> "Just now"
        s < 3600 -> "${(s + 30) / 60}m ago"
        s < 86400 -> "${(s + 1800) / 3600}h ago"
        s < 172800 -> "Yesterday"
        s < 604800 -> "${s / 86400}d ago"
        else -> java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(java.util.Date(millis))
    }
}

private val fenced = Regex("```[\\s\\S]*?```")
private val image = Regex("!\\[[^\\]]*]\\([^)]*\\)")
private val link = Regex("\\[([^\\]]+)]\\([^)]*\\)")
private val blockMarker = Regex("^\\s{0,3}(#{1,6}|>|[-*+]\\s+\\[[ xX]]|[-*+]|\\d+\\.)\\s+", RegexOption.MULTILINE)
private val inlineMarks = Regex("==|[*_~`]")

/** Plain-text snippet of a markdown body for list previews. */
fun plainPreview(md: String): String = md
    .replace(fenced, " ")
    .replace(image, "")
    .replace(link, "$1")
    .replace(blockMarker, "")
    .replace(inlineMarks, "")
    .replace(Regex("\\s+"), " ")
    .trim()
