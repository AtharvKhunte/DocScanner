package com.example.documentscanner.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {

    private const val KEYSTORE_ALIAS = "docvault_master_key"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val PREFS_NAME = "docvault_secure_prefs"
    private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_passphrase"
    private const val KEY_IV = "passphrase_iv"
    private const val GCM_TAG_LENGTH = 128

    /**
     * Returns the database passphrase, generating and encrypting a new one
     * on first run. Backed by a key that never leaves the Android Keystore.
     */
    fun getOrCreateDatabasePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existingEncrypted = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
        val existingIv = prefs.getString(KEY_IV, null)

        return if (existingEncrypted != null && existingIv != null) {
            decryptPassphrase(existingEncrypted, existingIv)
        } else {
            val newPassphrase = ByteArray(32).apply { SecureRandom().nextBytes(this) }
            val (encrypted, iv) = encryptPassphrase(newPassphrase)
            prefs.edit()
                .putString(KEY_ENCRYPTED_PASSPHRASE, encrypted)
                .putString(KEY_IV, iv)
                .apply()
            newPassphrase
        }
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

        keyStore.getKey(KEYSTORE_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptPassphrase(passphrase: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        val encrypted = cipher.doFinal(passphrase)
        val iv = cipher.iv

        return Pair(
            Base64.encodeToString(encrypted, Base64.NO_WRAP),
            Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    private fun decryptPassphrase(encryptedB64: String, ivB64: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(ivB64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeystoreKey(), spec)
        return cipher.doFinal(Base64.decode(encryptedB64, Base64.NO_WRAP))
    }
}