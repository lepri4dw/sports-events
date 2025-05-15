package com.example.sportevents.data.network
import android.util.Log
import com.example.sportevents.util.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    private val TAG = "AuthInterceptor"
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login and register requests
        val path = originalRequest.url.encodedPath
        if (path.contains("login") || path.contains("register") || path.contains("token/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Add authentication header to all other requests
        return try {
            val accessToken = AuthManager.getAccessToken()
            if (accessToken != null) {
                val authenticatedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(authenticatedRequest)
            } else {
                Log.d(TAG, "No access token available, proceeding without authentication")
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding authentication header: ${e.message}")
            chain.proceed(originalRequest)
        }
    }
}