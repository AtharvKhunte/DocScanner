package com.example.documentscanner.utils

import android.content.Context

object AppLockManager {
    private const val PREFS_NAME = "docvault_lock_prefs"
    private const val KEY_SETUP_COMPLETE = "setup_complete"

    // Settings prefs — same key as SettingsScreen uses
    private const val PREFS_SETTINGS = "docvault_settings"
    private const val KEY_BIOMETRIC_LOCK = "biometric_lock_enabled"

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

    fun isBiometricLockEnabled(context: Context): Boolean {
        // Defaults to true — lock is on unless user explicitly turned it off in settings
        return context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
            .getBoolean(KEY_BIOMETRIC_LOCK, true)
    }
}