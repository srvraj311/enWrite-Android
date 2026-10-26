package com.srvraj311.ui.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.srvraj311.data.Note
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object Export {
    /** The note as a markdown document: title heading, then the body with pasted images inlined. */
    fun markdown(note: Note): String =
        (if (note.title.isNotBlank()) "# ${note.title}\n\n" else "") +
            note.body.replace(Regex("]\\(img:([\\w-]+)\\)")) { m -> note.images[m.groupValues[1]]?.let { "]($it)" } ?: m.value }

    fun fileName(note: Note) = note.title.ifBlank { "Untitled" }.replace(Regex("[\\\\/:*?\"<>|]+"), "-")

    fun share(context: Context, note: Note) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, note.title.ifBlank { "Untitled" })
            putExtra(Intent.EXTRA_TEXT, markdown(note))
        }
        context.startActivity(Intent.createChooser(send, "Share note"))
    }

    private var printView: WebView? = null

    /** Opens the system print dialog, where "Save as PDF" is one of the destinations. */
    fun pdf(activity: Activity, note: Note) {
        val parser = Parser.builder().extensions(listOf(TablesExtension.create(), StrikethroughExtension.create())).build()
        val renderer = HtmlRenderer.builder().extensions(listOf(TablesExtension.create(), StrikethroughExtension.create())).softbreak("<br>").build()
        var body = renderer.render(parser.parse(markdown(note).removePrefix(if (note.title.isNotBlank()) "# ${note.title}\n\n" else "")))
        body = body.replace(Regex("==(.+?)=="), "<mark>$1</mark>")
            .replace(Regex("\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]", RegexOption.IGNORE_CASE)) { "<b>${it.groupValues[1].lowercase().replaceFirstChar { c -> c.uppercase() }}</b>" }
            .replace(Regex("<a href=\"enwrite://note/[^\"]*\"[^>]*>"), "<a>")
            .replace(Regex("<input[^>]*type=\"checkbox\"[^>]*>"), "")
        val html = """
            <html><head><meta charset="utf-8"><style>
            body{font:12pt/1.65 sans-serif;color:#19171c;margin:0}
            h1.t{font-size:26pt;border-bottom:3px solid #ef0a6a;padding-bottom:8pt;margin:0 0 14pt}
            h1,h2,h3{line-height:1.25} h2{border-bottom:1px solid #e7e4ea;padding-bottom:.2em}
            a{color:#ef0a6a} code{font-family:monospace;background:#f3f1f6;padding:.1em .3em;border-radius:4px}
            pre{background:#f6f4f9;border:1px solid #e7e4ea;border-radius:8px;padding:10pt;white-space:pre-wrap} pre code{background:none;padding:0}
            blockquote{margin:0 0 .9em;padding:.3em 1em;border-left:3px solid #ef0a6a;background:#fdf0f6}
            table{border-collapse:collapse} th,td{border:1px solid #d6d2db;padding:5pt 9pt} th{background:#f6f4f9}
            mark{background:#ffe58a} img{max-width:100%;border-radius:6px} hr{border:0;border-top:1px solid #e7e4ea}
            </style></head><body>${if (note.title.isNotBlank()) "<h1 class=\"t\">${android.text.Html.escapeHtml(note.title)}</h1>" else ""}$body</body></html>
        """.trimIndent()
        val web = WebView(activity)
        printView = web
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String?) {
                val manager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val name = fileName(note)
                manager.print(name, view.createPrintDocumentAdapter(name), PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build())
            }
        }
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
    }
}
