package com.example.documentscanner.utils

import android.app.Activity
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

object DocumentScannerLauncher {

    /**
     * Configured scanner: JPEG pages only (we don't need ML Kit's own PDF output
     * since we build our own PDFs via PdfExporter), unlimited pages, base scanning
     * mode (edge detection + crop + cleanup, no extra paid features).
     */
    val scannerOptions: GmsDocumentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(20)
        .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
        .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
        .build()

    fun extractImagePaths(result: GmsDocumentScanningResult): List<String> {
        return result.pages?.mapNotNull { page -> page.imageUri.path } ?: emptyList()
    }
}