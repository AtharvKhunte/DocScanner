package com.example.documentscanner.utils

import android.content.Context
import android.util.Log
import com.example.documentscanner.data.entity.ScannedDocument
import com.example.documentscanner.data.entity.pageList
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

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
    private val gotrue get() = client.gotrue
    private val db get() = client.postgrest
    private val storage get() = client.storage

    // ── Upload single document ────────────────────────────────────
    suspend fun uploadDocument(
        context: Context,
        document: ScannedDocument
    ): Result<Unit> {
        return try {
            val userId = gotrue.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))

            val pages = document.pageList()

            // Upload page images to Supabase Storage
            pages.forEachIndexed { index, pagePath ->
                val file = File(pagePath)
                if (file.exists()) {
                    val path = "$userId/${document.id}/page_$index.jpg"
                    try {
                        storage["document-images"].upload(
                            path = path,
                            data = file.readBytes(),
                            upsert = true
                        )
                    } catch (e: Exception) {
                        Log.w("SyncRepository", "Image upload failed: ${e.message}")
                    }
                }
            }

            // Upload metadata to Supabase DB
            val remote = RemoteDocument(
                localId = document.id,
                fileName = document.fileName,
                extractedText = document.extractedText,
                dateCreated = document.dateCreated,
                dateModified = document.dateModified,
                pageCount = pages.size
            )

            db["documents"].insert(remote, upsert = true)

            Log.d("SyncRepository", "Uploaded: ${document.fileName}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e("SyncRepository", "Upload failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Fetch all remote documents ────────────────────────────────
    suspend fun fetchRemoteDocuments(): Result<List<RemoteDocument>> {
        return try {
            val result = db["documents"]
                .select()
                .decodeList<RemoteDocument>()
            Result.success(result)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Fetch failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // ── Upload all local documents ────────────────────────────────
    suspend fun uploadAllDocuments(
        context: Context,
        documents: List<ScannedDocument>
    ): Result<Int> {
        var uploaded = 0
        documents.forEach { doc ->
            uploadDocument(context, doc).onSuccess { uploaded++ }
        }
        return Result.success(uploaded)
    }

    // ── Delete document from cloud ────────────────────────────────
    suspend fun deleteRemoteDocument(localId: Int): Result<Unit> {
        return try {
            val userId = gotrue.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Not logged in"))

            // Delete from DB
            db["documents"].delete {
                eq("local_id", localId)
            }

            // Delete images from storage
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

    // ── Get sync count ────────────────────────────────────────────
    suspend fun getSyncedCount(): Int {
        return try {
            db["documents"]
                .select()
                .decodeList<RemoteDocument>()
                .size
        } catch (e: Exception) { 0 }
    }
}