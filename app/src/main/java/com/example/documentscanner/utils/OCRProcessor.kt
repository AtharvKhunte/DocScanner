package com.example.documentscanner.utils

import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class OCRProcessor {

    private val textRecognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    suspend fun extractTextFromImage(imagePath: String): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap == null) {
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val inputImage = InputImage.fromBitmap(bitmap, 0)

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        try {
                            var extractedText = ""
                            for (block in visionText.textBlocks) {
                                for (line in block.lines) {
                                    for (element in line.elements) {
                                        extractedText += element.text + " "
                                    }
                                    extractedText += "\n"
                                }
                                extractedText += "\n"
                            }
                            bitmap.recycle()
                            val finalText = extractedText.trim()
                            continuation.resume(finalText.ifEmpty { null })
                        } catch (e: Exception) {
                            Log.e("OCRProcessor", "Error: ${e.message}", e)
                            bitmap.recycle()
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("OCRProcessor", "OCR failed: ${e.message}", e)
                        bitmap.recycle()
                        continuation.resume(null)
                    }
            } catch (e: Exception) {
                Log.e("OCRProcessor", "Exception: ${e.message}", e)
                continuation.resume(null)
            }
        }
    }

    /**
     * Runs OCR across multiple pages and combines the results,
     * labeling each page when there's more than one.
     */
    suspend fun extractTextFromImages(imagePaths: List<String>): String {
        val results = StringBuilder()
        imagePaths.forEachIndexed { index, path ->
            val text = extractTextFromImage(path)
            if (!text.isNullOrEmpty()) {
                if (imagePaths.size > 1) results.append("--- Page ${index + 1} ---\n")
                results.append(text).append("\n\n")
            }
        }
        return results.toString().trim()
    }
}