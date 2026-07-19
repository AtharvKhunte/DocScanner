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

    @Insert
    suspend fun insertDocument(document: ScannedDocument): Long

    @Query("SELECT * FROM scanned_documents ORDER BY dateCreated DESC")
    fun getAllDocuments(): Flow<List<ScannedDocument>>

    @Query("SELECT * FROM scanned_documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): ScannedDocument?

    @Query("SELECT * FROM scanned_documents WHERE fileName LIKE '%' || :query || '%' OR extractedText LIKE '%' || :query || '%'")
    fun searchDocuments(query: String): Flow<List<ScannedDocument>>

    @Query("DELETE FROM scanned_documents WHERE id = :id")
    suspend fun deleteDocument(id: Int)

    @Query("UPDATE scanned_documents SET extractedText = :text, dateModified = :modifiedAt WHERE id = :id")
    suspend fun updateExtractedText(id: Int, text: String, modifiedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM scanned_documents")
    suspend fun getDocumentCount(): Int

    @Query("DELETE FROM scanned_documents")
    suspend fun deleteAllDocuments()
}