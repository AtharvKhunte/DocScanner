package com.example.documentscanner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.documentscanner.data.entity.ScannedDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    // Insert a new document
    @Insert
    suspend fun insertDocument(document: ScannedDocument): Long

    // Get all documents (ordered by newest first)
    @Query("SELECT * FROM scanned_documents ORDER BY dateCreated DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    // Get a single document by ID
    @Query("SELECT * FROM scanned_documents WHERE id = :documentId")
    suspend fun getDocumentById(documentId: Int): ScannedDocument?

    // Search documents by extracted text
    @Query("SELECT * FROM scanned_documents WHERE extractedText LIKE '%' || :searchQuery || '%' ORDER BY dateCreated DESC")
    fun searchDocuments(searchQuery: String): Flow<List<ScannedDocument>>

    // Delete a document by ID
    @Query("DELETE FROM scanned_documents WHERE id = :documentId")
    suspend fun deleteDocument(documentId: Int)

    // Update extracted text for a document
    @Query("UPDATE scanned_documents SET extractedText = :text, dateModified = :timestamp WHERE id = :documentId")
    suspend fun updateExtractedText(documentId: Int, text: String, timestamp: Long = System.currentTimeMillis())

    // Get total document count
    @Query("SELECT COUNT(*) FROM scanned_documents")
    suspend fun getDocumentCount(): Int

    // Delete all documents
    @Query("DELETE FROM scanned_documents")
    suspend fun deleteAllDocuments()
}

/*
Query Explanations:

getAllDocuments():
- Returns all documents
- Ordered by newest first (DESC = descending)
- Flow = reactive - updates automatically if data changes

searchDocuments():
- Searches extractedText field
- Uses LIKE for partial matching
- Example: "Invoice" finds "Invoice #123"

updateExtractedText():
- Called after ML Kit extracts text
- Updates timestamp

Flow<T>:
- Reactive data stream
- UI automatically updates when data changes
- Perfect for LiveData alternatives
*/