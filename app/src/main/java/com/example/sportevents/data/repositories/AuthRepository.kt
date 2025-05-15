package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult

class AuthRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun registerUser(email: String, displayName: String, password: String): NetworkResult<AuthResponse> {
        val request = RegisterRequest(email, displayName, password)
        val response = safeApiCall { apiService.registerUser(request) }

        if (response is NetworkResult.Success) {
            AuthManager.saveTokens(response.data.access, response.data.refresh)
            AuthManager.saveUser(response.data.user)
        }

        return response
    }

    suspend fun loginUser(email: String, password: String): NetworkResult<AuthResponse> {
        val request = LoginRequest(email, password)
        val response = safeApiCall { apiService.loginUser(request) }

        if (response is NetworkResult.Success) {
            AuthManager.saveTokens(response.data.access, response.data.refresh)
            AuthManager.saveUser(response.data.user)
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
        return safeApiCall { apiService.getCurrentUser() }
    }

    suspend fun updateCurrentUser(updates: Map<String, Any>): NetworkResult<User> {
        val response = safeApiCall { apiService.updateCurrentUser(updates) }

        if (response is NetworkResult.Success) {
            AuthManager.saveUser(response.data)
        }

        return response
    }

    fun logout() {
        AuthManager.logout()
    }
}