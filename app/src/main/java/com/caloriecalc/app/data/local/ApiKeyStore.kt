package com.caloriecalc.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the user's own Gemini API key, hardware-backed-encrypted via the Android Keystore
 * (EncryptedSharedPreferences) rather than as plain text — this is a credential, not a setting.
 */
class ApiKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getGeminiApiKey(): String? = prefs.getString(KEY_GEMINI_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun setGeminiApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API_KEY, apiKey).apply()
    }

    fun clearGeminiApiKey() {
        prefs.edit().remove(KEY_GEMINI_API_KEY).apply()
    }

    companion object {
        private const val FILE_NAME = "caloriecalc_secure_prefs"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    }
}
