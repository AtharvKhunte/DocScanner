package com.example.documentscanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.RectF
import com.example.documentscanner.data.entity.ScannedDocument
import java.io.File
import java.io.FileOutputStream

object PdfExporter {

    private const val PAGE_WIDTH = 595  // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    fun exportToPdf(context: Context, document: ScannedDocument, includeText: Boolean = true): File? {
        return try {
            val pdfDocument = PdfDocument()

            // ===== PAGE 1: IMAGE =====
            val imagePageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val imagePage = pdfDocument.startPage(imagePageInfo)
            val imageCanvas = imagePage.canvas

            val bitmap = BitmapFactory.decodeFile(document.filePath)
            if (bitmap != null) {
                val availableWidth = PAGE_WIDTH - (MARGIN * 2)
                val availableHeight = PAGE_HEIGHT - (MARGIN * 2)

                val scale = minOf(
                    availableWidth / bitmap.width,
                    availableHeight / bitmap.height
                )
                val scaledWidth = bitmap.width * scale
                val scaledHeight = bitmap.height * scale

                val left = (PAGE_WIDTH - scaledWidth) / 2
                val top = (PAGE_HEIGHT - scaledHeight) / 2

                val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                imageCanvas.drawBitmap(bitmap, null, destRect, Paint(Paint.ANTI_ALIAS_FLAG))
                bitmap.recycle()
            }

            pdfDocument.finishPage(imagePage)

            // ===== PAGE 2: EXTRACTED TEXT =====
            if (includeText && document.extractedText.isNotEmpty()) {
                val textPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                val textPage = pdfDocument.startPage(textPageInfo)
                val textCanvas = textPage.canvas

                val titlePaint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                    isFakeBoldText = true
                }
                val bodyPaint = Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 11f
                }

                var yPosition = MARGIN + 20f
                textCanvas.drawText(document.fileName, MARGIN, yPosition, titlePaint)
                yPosition += 30f

                val maxWidth = PAGE_WIDTH - (MARGIN * 2)
                val lines = wrapText(document.extractedText, bodyPaint, maxWidth)

                for (line in lines) {
                    if (yPosition > PAGE_HEIGHT - MARGIN) break // avoid overflow (simple single-page cap)
                    textCanvas.drawText(line, MARGIN, yPosition, bodyPaint)
                    yPosition += 16f
                }

                pdfDocument.finishPage(textPage)
            }

            // ===== SAVE TO FILE =====
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val safeFileName = document.fileName.substringBeforeLast(".").ifEmpty { "document" }
            val outputFile = File(exportDir, "$safeFileName.pdf")

            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            outputFile
        } catch (e: Exception) {
            android.util.Log.e("PdfExporter", "Export failed: ${e.message}", e)
            null
        }
    }

    /**
     * Simple word-wrap for canvas text drawing.
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()

            for (word in words) {
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