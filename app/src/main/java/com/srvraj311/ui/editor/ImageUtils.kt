package com.srvraj311.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URL

/** A picked image, shrunk and encoded so it can live inside the note (`data:` URI). */
class ImportedImage(val name: String, val dataUri: String)

object ImageUtils {
    /** Firestore documents are capped at 1 MB, so each image is squeezed to stay well under that. */
    private const val LIMIT = 350 * 1024

    suspend fun import(context: Context, uri: Uri): ImportedImage? = withContext(Dispatchers.IO) {
        val name = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull()?.substringBeforeLast('.')?.replace(Regex("[\\[\\]]"), "")?.ifBlank { null } ?: "image"

        var bytes: ByteArray? = null
        for ((side, quality) in listOf(1400 to 82, 1100 to 76, 900 to 68, 700 to 60)) {
            val bitmap = decode(context, uri, side) ?: return@withContext null
            bytes = encode(bitmap, quality)
            bitmap.recycle()
            if (bytes.size <= LIMIT) break
        }
        val data = bytes ?: return@withContext null
        if (data.size > LIMIT * 1.5) return@withContext null
        ImportedImage(name, "data:image/webp;base64," + Base64.encodeToString(data, Base64.NO_WRAP))
    }

    private fun decode(context: Context, uri: Uri, maxSide: Int): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxSide * 2 || bounds.outHeight / sample > maxSide * 2) sample *= 2
        val raw = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = minOf(1f, maxSide / maxOf(raw.width, raw.height).toFloat())
        if (scale >= 1f) raw else Bitmap.createScaledBitmap(raw, (raw.width * scale).toInt(), (raw.height * scale).toInt(), true).also { if (it !== raw) raw.recycle() }
    }.getOrNull()

    @Suppress("DEPRECATION")
    private fun encode(bitmap: Bitmap, quality: Int): ByteArray = ByteArrayOutputStream().also {
        val format = if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
        bitmap.compress(format, quality, it)
    }.toByteArray()

    /** Raw bytes for saving an image, from a data URI or a web URL. */
    suspend fun bytesOf(source: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            if (source.startsWith("data:")) Base64.decode(source.substringAfter("base64,"), Base64.DEFAULT)
            else URL(source).openStream().use { it.readBytes() }
        }.getOrNull()
    }

    fun extensionOf(source: String) = when {
        source.startsWith("data:image/png") -> "png"
        source.startsWith("data:image/jpeg") -> "jpg"
        source.startsWith("data:image/gif") -> "gif"
        source.startsWith("data:") -> "webp"
        source.substringBefore('?').substringAfterLast('.', "").lowercase().let { it in setOf("png", "jpg", "jpeg", "gif", "webp") } ->
            source.substringBefore('?').substringAfterLast('.').lowercase()
        else -> "png"
    }
}
