package br.com.fiap.global.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun createSession(token: String, expirationTime: Long) {
        prefs.edit().apply {
            putString("session_token", token)
            putLong("session_expiration", expirationTime)
            apply()
        }
    }

    fun isSessionValid(): Boolean {
        val expirationTime = prefs.getLong("session_expiration", 0)
        return System.currentTimeMillis() < expirationTime
    }

    fun saveUserDetails(userDetails: Map<Any?, String>) {
        val editor = prefs.edit()
        for ((key, value) in userDetails) {
            if (key is String) {
                editor.putString(key, value)
            }
        }
        editor.apply()
    }

    fun getUserDetails(): Map<String, String> {
        val keys = prefs.all.keys
        val userDetails = mutableMapOf<String, String>()
        for (key in keys) {
            if (key != "session_token" && key != "session_expiration") {
                userDetails[key] = prefs.getString(key, "") ?: ""
            }
        }
        return userDetails
    }

    fun getSessionToken(): String? {
        return prefs.getString("session_token", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
