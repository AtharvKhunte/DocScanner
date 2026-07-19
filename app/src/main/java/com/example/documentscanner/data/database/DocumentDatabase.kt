package com.example.documentscanner.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.documentscanner.data.dao.DocumentDao
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.utils.KeystoreManager
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [ScannedDocument::class],
    version = 2,
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
            val passphrase = KeystoreManager.getOrCreateDatabasePassphrase(context)
            val factory = SupportFactory(passphrase)

            return Room.databaseBuilder(
                context.applicationContext,
                DocumentDatabase::class.java,
                "document_scanner.db"
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}