package com.example.documentscanner.domain.repository

import com.example.documentscanner.data.dao.DocumentDao
import com.example.documentscanner.data.entity.ScannedDocument
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val dao: DocumentDao) {

    fun getAllDocuments(): Flow<List<ScannedDocument>> = dao.getAllDocuments()

    suspend fun getDocumentById(id: Int): ScannedDocument? = dao.getDocumentById(id)

    suspend fun saveDocumentWithText(fileName: String, filePaths: String, extractedText: String): Long {
        val document = ScannedDocument(
            fileName = fileName,
            filePaths = filePaths,
            extractedText = extractedText
        )
        return dao.insertDocument(document)
    }

    suspend fun updateExtractedText(id: Int, text: String) = dao.updateExtractedText(id, text)

    fun searchDocuments(query: String): Flow<List<ScannedDocument>> = dao.searchDocuments(query)

    suspend fun deleteDocument(id: Int) = dao.deleteDocument(id)

    suspend fun getDocumentCount(): Int = dao.getDocumentCount()

    suspend fun deleteAllDocuments() = dao.deleteAllDocuments()
}