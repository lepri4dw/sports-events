package com.example.sportevents.util

import android.content.Context
import android.content.SharedPreferences
import com.example.sportevents.data.models.User
import com.google.gson.Gson

object AuthManager {
    private const val PREFS_NAME = "sport_events_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER = "current_user"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getCurrentUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            Gson().fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getCurrentUser() != null
    }

    fun logout() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER)
            apply()
        }
    }
}