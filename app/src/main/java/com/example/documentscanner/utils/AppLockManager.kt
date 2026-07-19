package com.example.documentscanner.utils

import android.content.Context

object AppLockManager {
    private const val PREFS_NAME = "docvault_lock_prefs"
    private const val KEY_SETUP_COMPLETE = "setup_complete"

    fun isSetupComplete(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SETUP_COMPLETE, false)
    }

    fun markSetupComplete(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SETUP_COMPLETE, true)
            .apply()
    }
}