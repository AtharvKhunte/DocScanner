package com.example.documentscanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.documentscanner.data.dao.DocumentDao
import com.example.documentscanner.data.entity.ScannedDocument
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [ScannedDocument::class],
    version = 1,
    exportSchema = false
)
abstract class DocumentDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var instance: DocumentDatabase? = null

        fun getInstance(context: Context): DocumentDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }
        }

        private fun createDatabase(context: Context): DocumentDatabase {
            // Create encrypted database using SQLCipher
            val passphrase = "DocumentScannerSecureKey123".toByteArray()
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                DocumentDatabase::class.java,
                "document_scanner.db"
            )
                .openHelperFactory(factory)  // SQLCipher encryption
                .build()
        }
    }
}

/*
Database Configuration Explained:

@Database:
- Specifies which entities exist (ScannedDocument)
- version = 1 (increment if schema changes)
- exportSchema = false (schema not exported to JSON)

getInstance():
- Singleton pattern (only one database instance)
- Thread-safe (synchronized)
- Creates on first access

createDatabase():
- Uses SQLCipher for encryption
- Passphrase protects the database
- File: document_scanner.db

Encryption:
- SQLCipher encrypts entire database
- Passphrase prevents unauthorized access
- Only readable by your app
*/