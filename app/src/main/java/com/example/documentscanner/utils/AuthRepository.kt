package com.example.documentscanner.utils

import android.util.Log
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.user.UserInfo

object AuthRepository {

    private val gotrue get() = SupabaseManager.client.gotrue

    val currentUser: UserInfo?
        get() = gotrue.currentUserOrNull()

    val isLoggedIn: Boolean
        get() = gotrue.currentUserOrNull() != null

    val userEmail: String
        get() = gotrue.currentUserOrNull()?.email ?: ""

    suspend fun signOut(): Result<Unit> {
        return try {
            gotrue.logout()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign out error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun refreshSession(): Result<Unit> {
        return try {
            gotrue.refreshCurrentSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Refresh error: ${e.message}", e)
            Result.failure(e)
        }
    }
}