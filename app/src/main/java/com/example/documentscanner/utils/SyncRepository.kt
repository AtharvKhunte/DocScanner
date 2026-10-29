package com.example.documentscanner.utils

import android.content.Context
import android.util.Log
import com.example.documentscanner.data.entity.ScannedDocument
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import com.example.documentscanner.data.entity.pageList

@Serializable
data class RemoteDocument(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("local_id") val localId: Int,
    @SerialName("file_name") val fileName: String,
    @SerialName("extracted_text") val extractedText: String = "",
    @SerialName("date_created") val dateCreated: Long,
    @SerialName("date_modified") val dateModified: Long,
    @SerialName("page_count") val pageCount: Int = 1
)

object SyncRepository {

    private val client get() = SupabaseManager.client
    private val auth get() = client.auth
    private val db get() = client.postgrest
    private val storage get() = client.storage

    suspend fun uploadDocument(
        context: Context,
        document: ScannedDocument
    ): Result<Unit> {
        return try {
            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))

            val pages = document.pageList()

            pages.forEachIndexed { index, pagePath ->
                val file = File(pagePath)
                if (file.exists()) {
                    val path = "$userId/${document.id}/page_$index.jpg"
                    try {
                        storage["document-images"].upload(path, file.readBytes()) {
                            upsert = true
                        }
                    } catch (e: Exception) {
                        Log.w("SyncRepository", "Image upload failed: ${e.message}")
                    }
                }
            }

            val remote = RemoteDocument(
                localId = document.id,
                fileName = document.fileName,
                extractedText = document.extractedText,
                dateCreated = document.dateCreated,
                dateModified = document.dateModified,
                pageCount = pages.size
            )

            db.from("documents").upsert(remote)

            Log.d("SyncRepository", "Uploaded: ${document.fileName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Upload failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchRemoteDocuments(): Result<List<RemoteDocument>> {
        return try {
            val result = db.from("documents")
                .select()
                .decodeList<RemoteDocument>()
            Result.success(result)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Fetch failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteDocument(localId: Int): Result<Unit> {
        return try {
            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))

            db.from("documents").delete {
                filter {
                    eq("local_id", localId)
                }
            }

            try {
                val files = storage["document-images"].list("$userId/$localId/")
                val paths = files.map { "$userId/$localId/${it.name}" }
                if (paths.isNotEmpty()) {
                    storage["document-images"].delete(paths)
                }
            } catch (e: Exception) {
                Log.w("SyncRepository", "Storage cleanup: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Delete failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getSyncedCount(): Int {
        return try {
            db.from("documents")
                .select()
                .decodeList<RemoteDocument>()
                .size
        } catch (e: Exception) { 0 }
    }
}