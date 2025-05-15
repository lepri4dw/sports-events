package com.example.sportevents.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.sportevents.data.models.User
import com.google.gson.Gson

object AuthManager {
    private const val PREFS_NAME = "sport_events_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER = "current_user"
    private const val TAG = "AuthManager"

    private var _prefs: SharedPreferences? = null
    private val prefs: SharedPreferences
        get() {
            if (_prefs == null) {
                Log.e(TAG, "AuthManager not initialized. Make sure to call init() first.")
                throw UninitializedPropertyAccessException("AuthManager not initialized. Make sure to call init() first.")
            }
            return _prefs!!
        }

    fun init(context: Context) {
        if (_prefs == null) {
            _prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d(TAG, "AuthManager initialized")
        }
    }

    fun isInitialized(): Boolean = _prefs != null

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
        return try {
            prefs.getString(KEY_ACCESS_TOKEN, null)
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to get access token: ${e.message}")
            null
        }
    }

    fun getRefreshToken(): String? {
        return try {
            prefs.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to get refresh token: ${e.message}")
            null
        }
    }

    fun getCurrentUser(): User? {
        try {
            val userJson = prefs.getString(KEY_USER, null) ?: return null
            return try {
                Gson().fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse user JSON: ${e.message}")
                null
            }
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to get current user: ${e.message}")
            return null
        }
    }

    fun isLoggedIn(): Boolean {
        return try {
            getAccessToken() != null && getCurrentUser() != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking login status: ${e.message}")
            false
        }
    }

    fun logout() {
        try {
            prefs.edit().apply {
                remove(KEY_ACCESS_TOKEN)
                remove(KEY_REFRESH_TOKEN)
                remove(KEY_USER)
                apply()
            }
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to logout: ${e.message}")
        }
    }
}