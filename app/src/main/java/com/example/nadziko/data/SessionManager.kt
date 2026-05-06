package com.example.nadziko.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nadziko_session", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val USERNAME = "username"
    }

    fun saveSession(userId: Int, username: String) {
        prefs.edit().apply {
            putInt(USER_ID, userId)
            putString(USERNAME, username)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt(USER_ID, -1)
    fun getUsername(): String? = prefs.getString(USERNAME, null)

    fun isLoggedIn(): Boolean = getUserId() != -1

    fun logout() {
        prefs.edit().clear().apply()
    }
}
