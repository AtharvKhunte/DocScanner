package com.example.documentscanner.utils

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo

object AuthRepository {

    private val auth get() = SupabaseManager.client.auth

    val currentUser: UserInfo?
        get() = auth.currentUserOrNull()

    val isLoggedIn: Boolean
        get() = auth.currentUserOrNull() != null

    val userEmail: String
        get() = auth.currentUserOrNull()?.email ?: ""

    suspend fun restoreSession(): Boolean {
        return try {
            auth.awaitInitialization()
            auth.currentUserOrNull() != null
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session restore failed: ${e.message}")
            false
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign out error: ${e.message}", e)
            Result.failure(e)
        }
    }
}