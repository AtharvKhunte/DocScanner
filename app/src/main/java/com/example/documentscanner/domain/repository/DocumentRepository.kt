package com.example.documentscanner.domain.repository

import com.example.documentscanner.data.dao.DocumentDao
import com.example.documentscanner.data.entity.ScannedDocument
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {

    // Get all documents as a reactive stream
    fun getAllDocuments(): Flow<List<ScannedDocument>> {
        return documentDao.getAllDocuments()
    }

    // Get a single document by ID
    suspend fun getDocumentById(documentId: Int): ScannedDocument? {
        return documentDao.getDocumentById(documentId)
    }

    // Save a new document
    suspend fun saveDocument(document: ScannedDocument): Long {
        android.util.Log.d("DocumentRepository", "Saving document: ${document.fileName}")
        return documentDao.insertDocument(document)
    }

    // Save document with extracted text
    suspend fun saveDocumentWithText(
        fileName: String,
        filePath: String,
        extractedText: String
    ): Long {
        val document = ScannedDocument(
            fileName = fileName,
            filePath = filePath,
            extractedText = extractedText,
            dateCreated = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
        return saveDocument(document)
    }

    // Update extracted text (called after OCR)
    suspend fun updateExtractedText(documentId: Int, text: String) {
        android.util.Log.d("DocumentRepository", "Updating text for document $documentId")
        documentDao.updateExtractedText(documentId, text)
    }

    // Search documents by text
    fun searchDocuments(query: String): Flow<List<ScannedDocument>> {
        return if (query.isEmpty()) {
            getAllDocuments()
        } else {
            documentDao.searchDocuments(query)
        }
    }

    // Delete a document
    suspend fun deleteDocument(documentId: Int) {
        android.util.Log.d("DocumentRepository", "Deleting document $documentId")
        documentDao.deleteDocument(documentId)
    }

    // Get total document count
    suspend fun getDocumentCount(): Int {
        return documentDao.getDocumentCount()
    }

    // Delete all documents
    suspend fun deleteAllDocuments() {
        android.util.Log.d("DocumentRepository", "Deleting all documents")
        documentDao.deleteAllDocuments()
    }
}

/*
Repository Pattern Explained:

Why use Repository?
- Abstracts database implementation
- Makes UI code cleaner
- Easier to mock for testing
- Single source of truth for data

Usage in UI:
Instead of: documentDao.insertDocument(...)
Use: repository.saveDocument(...)

Flow Benefits:
- Reactive data binding
- Automatic UI updates
- No manual refresh needed
*/