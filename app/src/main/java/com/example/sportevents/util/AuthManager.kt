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
        Log.d(TAG, "Токены сохранены: access=${accessToken.take(15)}..., refresh=${refreshToken.take(15)}...")
    }

    fun saveUser(user: User) {
        val userJson = Gson().toJson(user)
        prefs.edit().putString(KEY_USER, userJson).apply()
    }
    
    // Алиас для saveUser для обратной совместимости
    fun setCurrentUser(user: User) {
        saveUser(user)
    }

    fun getAccessToken(): String? {
        return try {
            val token = prefs.getString(KEY_ACCESS_TOKEN, null)
            Log.d(TAG, "Получен access token: ${token?.take(15)}...")
            token
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to get access token: ${e.message}")
            null
        }
    }

    fun getRefreshToken(): String? {
        return try {
            val token = prefs.getString(KEY_REFRESH_TOKEN, null)
            Log.d(TAG, "Получен refresh token: ${token?.take(15)}...")
            token
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
            val isLoggedIn = getAccessToken() != null && getCurrentUser() != null
            Log.d(TAG, "Проверка статуса логина: $isLoggedIn")
            isLoggedIn
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
            Log.d(TAG, "Пользователь вышел из системы, токены удалены")
        } catch (e: UninitializedPropertyAccessException) {
            Log.e(TAG, "Failed to logout: ${e.message}")
        }
    }
}