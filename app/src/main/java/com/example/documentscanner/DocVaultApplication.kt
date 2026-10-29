package com.example.documentscanner

import android.app.Application
import com.example.documentscanner.utils.SupabaseManager

class DocVaultApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseManager.initialize(this)
    }
}