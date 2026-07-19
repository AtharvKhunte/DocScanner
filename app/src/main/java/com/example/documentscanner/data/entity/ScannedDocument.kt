package com.example.documentscanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_documents")
data class ScannedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,
    val filePaths: String, // comma-separated paths, one per page
    val extractedText: String = "",
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
)

fun ScannedDocument.pageList(): List<String> =
    filePaths.split(",").map { it.trim() }.filter { it.isNotBlank() }