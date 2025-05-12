package com.example.sportevents.data.network
import com.example.sportevents.util.AuthManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login and register requests
        val path = originalRequest.url.encodedPath
        if (path.contains("login") || path.contains("register") || path.contains("token/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Add authentication header to all other requests
        val accessToken = AuthManager.getAccessToken()
        return if (accessToken != null) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}