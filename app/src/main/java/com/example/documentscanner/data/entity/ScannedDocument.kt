package com.example.documentscanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scanned_documents")
data class ScannedDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val fileName: String,           // e.g., "photo_20260508_190122.jpg"
    val filePath: String,           // Full path: /storage/emulated/0/Android/data/...
    val extractedText: String = "", // Text extracted from image
    val dateCreated: Long = System.currentTimeMillis(),  // Timestamp
    val dateModified: Long = System.currentTimeMillis()
)

/*
Database Table Structure:
┌────────────────┬──────────────┬────────────────┐
│ Column         │ Type         │ Notes          │
├────────────────┼──────────────┼────────────────┤
│ id             │ INTEGER      │ Primary Key    │
│ fileName       │ TEXT         │ File name      │
│ filePath       │ TEXT         │ Full path      │
│ extractedText  │ TEXT         │ OCR result     │
│ dateCreated    │ INTEGER      │ Timestamp      │
│ dateModified   │ INTEGER      │ Timestamp      │
└────────────────┴──────────────┴────────────────┘
*/