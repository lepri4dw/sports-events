package com.example.sportevents.data.repositories

import android.util.Log
import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult

class AuthRepository : BaseRepository() {
    private val TAG = "AuthRepository"
    private val apiService = RetrofitClient.apiService

    suspend fun registerUser(email: String, displayName: String, password: String): NetworkResult<AuthResponse> {
        val request = RegisterRequest(email, displayName, password)
        val response = safeApiCall { apiService.registerUser(request) }

        if (response is NetworkResult.Success) {
            AuthManager.saveTokens(response.data.access, response.data.refresh)
            AuthManager.setCurrentUser(response.data.user)
        }

        return response
    }

    suspend fun loginUser(email: String, password: String): NetworkResult<AuthResponse> {
        val request = LoginRequest(email, password)
        val response = safeApiCall { apiService.loginUser(request) }

        if (response is NetworkResult.Success) {
            AuthManager.saveTokens(response.data.access, response.data.refresh)
            AuthManager.setCurrentUser(response.data.user)
        }

        return response
    }

    suspend fun refreshToken(refreshToken: String): NetworkResult<TokenRefreshResponse> {
        val request = TokenRefreshRequest(refreshToken)
        val response = safeApiCall { apiService.refreshToken(request) }

        if (response is NetworkResult.Success) {
            AuthManager.saveTokens(response.data.access, AuthManager.getRefreshToken()!!)
        }

        return response
    }

    suspend fun getCurrentUser(): NetworkResult<User> {
        return safeApiCall { apiService.getCurrentUser() }.also { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Successfully fetched current user: ${result.data.display_name}")
                    AuthManager.setCurrentUser(result.data)
                }
                is NetworkResult.Error -> Log.e(TAG, "Error fetching current user: ${result.message}")
                is NetworkResult.Loading -> Log.d(TAG, "Loading current user...")
            }
        }
    }

    suspend fun updateCurrentUser(updates: Map<String, Any>): NetworkResult<User> {
        return safeApiCall { apiService.updateCurrentUser(updates) }.also { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Successfully updated current user: ${result.data.display_name}")
                    AuthManager.setCurrentUser(result.data)
                }
                is NetworkResult.Error -> Log.e(TAG, "Error updating current user: ${result.message}")
                is NetworkResult.Loading -> Log.d(TAG, "Updating current user...")
            }
        }
    }

    fun logout() {
        AuthManager.logout()
    }
}