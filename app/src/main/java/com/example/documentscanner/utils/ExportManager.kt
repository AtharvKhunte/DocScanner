package com.example.documentscanner.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ExportManager {

    private const val DOCVAULT_FOLDER = "DocVault"
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    // ============================================================
    // PDF EXPORT
    // ============================================================

    fun exportPdf(
        context: Context,
        document: ScannedDocument,
        includeText: Boolean
    ): ExportResult {
        return try {
            val fileName = buildFileName(document.fileName, "pdf")
            val pdfBytes = buildPdfBytes(document, includeText)

            val uri = saveToDownloads(context, fileName, "application/pdf", pdfBytes)
                ?: return ExportResult.Error("Failed to save PDF")

            ExportResult.Success(uri, fileName, "pdf")
        } catch (e: Exception) {
            android.util.Log.e("ExportManager", "PDF export failed: ${e.message}", e)
            ExportResult.Error(e.message ?: "Unknown error")
        }
    }

    // ============================================================
    // TXT EXPORT
    // ============================================================

    fun exportTxt(
        context: Context,
        document: ScannedDocument
    ): ExportResult {
        return try {
            if (document.extractedText.isBlank()) {
                return ExportResult.Error("No text extracted yet")
            }
            val fileName = buildFileName(document.fileName, "txt")
            val bytes = document.extractedText.toByteArray(Charsets.UTF_8)

            val uri = saveToDownloads(context, fileName, "text/plain", bytes)
                ?: return ExportResult.Error("Failed to save TXT")

            ExportResult.Success(uri, fileName, "txt")
        } catch (e: Exception) {
            android.util.Log.e("ExportManager", "TXT export failed: ${e.message}", e)
            ExportResult.Error(e.message ?: "Unknown error")
        }
    }

    // ============================================================
    // SHARE
    // ============================================================

    fun share(context: Context, uri: Uri, mimeType: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share"))
    }

    // ============================================================
    // LIST EXPORTS (for Exports screen)
    // ============================================================

    fun listExports(context: Context): List<ExportedFile> {
        val results = mutableListOf<ExportedFile>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val projection = arrayOf(
                MediaStore.Downloads._ID,
                MediaStore.Downloads.DISPLAY_NAME,
                MediaStore.Downloads.SIZE,
                MediaStore.Downloads.DATE_ADDED,
                MediaStore.Downloads.MIME_TYPE
            )
            val selection = "${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("%$DOCVAULT_FOLDER%")

            context.contentResolver.query(
                collection, projection, selection, selectionArgs,
                "${MediaStore.Downloads.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_ADDED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = Uri.withAppendedPath(collection, id.toString())
                    results.add(
                        ExportedFile(
                            uri = uri,
                            name = cursor.getString(nameCol) ?: "",
                            sizeBytes = cursor.getLong(sizeCol),
                            dateAdded = cursor.getLong(dateCol) * 1000L,
                            mimeType = cursor.getString(mimeCol) ?: "application/pdf"
                        )
                    )
                }
            }
        } else {
            // API < 29: read directly from file system
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DOCVAULT_FOLDER
            )
            if (dir.exists()) {
                dir.listFiles()?.sortedByDescending { it.lastModified() }?.forEach { file ->
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", file
                    )
                    results.add(
                        ExportedFile(
                            uri = uri,
                            name = file.name,
                            sizeBytes = file.length(),
                            dateAdded = file.lastModified(),
                            mimeType = if (file.extension == "pdf") "application/pdf" else "text/plain"
                        )
                    )
                }
            }
        }

        return results
    }

    // ============================================================
    // DELETE EXPORT
    // ============================================================

    fun deleteExport(context: Context, uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            android.util.Log.e("ExportManager", "Delete failed: ${e.message}", e)
        }
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private fun saveToDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH,
                    "${Environment.DIRECTORY_DOWNLOADS}/$DOCVAULT_FOLDER")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val uri = context.contentResolver.insert(collection, values) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(bytes)
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            uri
        } else {
            // API < 29 fallback
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                DOCVAULT_FOLDER
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        }
    }

    private fun buildFileName(originalName: String, extension: String): String {
        val base = originalName.substringBeforeLast(".").ifEmpty { "document" }
        val timestamp = java.text.SimpleDateFormat(
            "yyyyMMdd_HHmmss", java.util.Locale.US
        ).format(java.util.Date())
        return "${base}_$timestamp.$extension"
    }

    private fun buildPdfBytes(document: ScannedDocument, includeText: Boolean): ByteArray {
        val pdfDocument = PdfDocument()
        val pages = document.pageList()

        pages.forEachIndexed { index, imagePath ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val bitmap = decodeSampledBitmap(imagePath, 1600)
            if (bitmap != null) {
                val availableWidth = PAGE_WIDTH - (MARGIN * 2)
                val availableHeight = PAGE_HEIGHT - (MARGIN * 2)
                val scale = minOf(availableWidth / bitmap.width, availableHeight / bitmap.height)
                val scaledWidth = bitmap.width * scale
                val scaledHeight = bitmap.height * scale
                val left = (PAGE_WIDTH - scaledWidth) / 2
                val top = (PAGE_HEIGHT - scaledHeight) / 2
                canvas.drawBitmap(bitmap, null, RectF(left, top, left + scaledWidth, top + scaledHeight), Paint(Paint.ANTI_ALIAS_FLAG))
                bitmap.recycle()
            }
            pdfDocument.finishPage(page)
        }

        if (includeText && document.extractedText.isNotEmpty()) {
            val textPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pages.size + 1).create()
            val textPage = pdfDocument.startPage(textPageInfo)
            val canvas = textPage.canvas

            val titlePaint = Paint().apply { color = android.graphics.Color.BLACK; textSize = 16f; isFakeBoldText = true }
            val bodyPaint = Paint().apply { color = android.graphics.Color.DKGRAY; textSize = 11f }

            var y = MARGIN + 20f
            canvas.drawText(document.fileName, MARGIN, y, titlePaint)
            y += 30f

            val maxWidth = PAGE_WIDTH - (MARGIN * 2)
            wrapText(document.extractedText, bodyPaint, maxWidth).forEach { line ->
                if (y <= PAGE_HEIGHT - MARGIN) {
                    canvas.drawText(line, MARGIN, y, bodyPaint)
                    y += 16f
                }
            }
            pdfDocument.finishPage(textPage)
        }

        val outputStream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        return outputStream.toByteArray()
    }

    private fun decodeSampledBitmap(imagePath: String, maxDimension: Int): android.graphics.Bitmap? {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, boundsOptions)
        var sampleSize = 1
        val longestSide = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
        while (longestSide / sampleSize > maxDimension) sampleSize *= 2
        return BitmapFactory.decodeFile(imagePath, BitmapFactory.Options().apply { inSampleSize = sampleSize })
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        text.split("\n").forEach { paragraph ->
            if (paragraph.isEmpty()) { lines.add(""); return@forEach }
            var currentLine = StringBuilder()
            paragraph.split(" ").forEach { word ->
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = StringBuilder(testLine)
                } else {
                    if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        }
        return lines
    }
}

// ============================================================
// DATA CLASSES
// ============================================================

data class ExportedFile(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val dateAdded: Long,
    val mimeType: String
) {
    val ispdf: Boolean get() = mimeType == "application/pdf"
    val formattedSize: String get() {
        return when {
            sizeBytes < 1024 -> "${sizeBytes}B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024}KB"
            else -> "${"%.1f".format(sizeBytes / (1024f * 1024f))}MB"
        }
    }
}

sealed class ExportResult {
    data class Success(val uri: Uri, val fileName: String, val type: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}