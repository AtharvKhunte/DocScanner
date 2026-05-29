package com.example.documentscanner.utils

import android.graphics.Bitmap
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
                Log.d("OCRProcessor", "Starting OCR for: $imagePath")

                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    Log.e("OCRProcessor", "Image file not found: $imagePath")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap == null) {
                    Log.e("OCRProcessor", "Failed to decode image")
                    continuation.resume(null)
                    return@suspendCancellableCoroutine
                }

                Log.d("OCRProcessor", "Image loaded: ${bitmap.width}x${bitmap.height}")

                val inputImage = InputImage.fromBitmap(bitmap, 0)

                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        try {
                            Log.d("OCRProcessor", "OCR completed successfully")

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

                            if (finalText.isEmpty()) {
                                Log.w("OCRProcessor", "No text found in image")
                                continuation.resume(null)
                            } else {
                                Log.d("OCRProcessor", "Extracted: ${finalText.length} characters")
                                continuation.resume(finalText)
                            }
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
}