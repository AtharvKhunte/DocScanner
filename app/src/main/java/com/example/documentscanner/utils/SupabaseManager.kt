package com.example.documentscanner.utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    val client = createSupabaseClient(
        supabaseUrl = "https://cignobhbxotmhwlfhhvq.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNpZ25vYmhieG90bWh3bGZoaHZxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYxODI5NjAsImV4cCI6MjEwMTc1ODk2MH0.O1vMCkKCAsdCTu7yAxMi_w4gRMeAEkED3xmRq0AjobE"
    ) {
        install(GoTrue) {
            scheme = "docvault"
            host = "auth"
        }
        install(Postgrest)
        install(Storage)
        install(ComposeAuth) {
            googleNativeLogin(
                serverClientId = "925605241455-ivjomq3svh0d6447s2f8c3brb5bp5isa.apps.googleusercontent.com"
            )
        }
    }
}