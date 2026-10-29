package com.example.documentscanner.utils

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SettingsSessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    private var _client: io.github.jan.supabase.SupabaseClient? = null

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
        val settings = SharedPreferencesSettings(prefs)

        _client = createSupabaseClient(
            supabaseUrl = "https://cignobhbxotmhwlfhhvq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNpZ25vYmhieG90bWh3bGZoaHZxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYxODI5NjAsImV4cCI6MjEwMTc1ODk2MH0.O1vMCkKCAsdCTu7yAxMi_w4gRMeAEkED3xmRq0AjobE"
        ) {
            install(Auth) {
                scheme = "docvault"
                host = "auth"
                sessionManager = SettingsSessionManager(settings)
            }
            install(Postgrest)
            install(Storage)
        }
    }

    val client get() = _client
        ?: error("SupabaseManager not initialized")
}