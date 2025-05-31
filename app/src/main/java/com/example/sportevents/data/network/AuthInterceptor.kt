package com.example.sportevents.data.network
import android.util.Log
import com.example.sportevents.data.models.TokenRefreshRequest
import com.example.sportevents.util.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class AuthInterceptor : Interceptor {
    private val TAG = "AuthInterceptor"
    private var isRefreshing = false
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip authentication for login, register, and token refresh requests
        val path = originalRequest.url.encodedPath
        
        // Исправленная проверка путей - теперь проверяем точные пути для публичных запросов
        if (path.contains("/api/auth/login") || 
            path.contains("/api/auth/register") || 
            path.contains("/api/auth/token/refresh")) {
            
            Log.d(TAG, "Запрос без аутентификации: $path")
            return chain.proceed(originalRequest)
        }

        // Add authentication header to all other requests
        // Добавляем явный лог
        Log.d(TAG, "Запрос требует аутентификации: $path")
        
        return try {
            var accessToken = AuthManager.getAccessToken()
            
            // Добавить лог для проверки полученного токена
            Log.d(TAG, "Получен токен из AuthManager: ${accessToken?.take(15)}...")
            
            // If we have a token, add it to the request
            if (accessToken != null) {
                val authenticatedRequest = addAuthHeader(originalRequest, accessToken)
                // Добавить лог с URL и заголовками запроса
                Log.d(TAG, "Запрос с аутентификацией: ${authenticatedRequest.url}, метод: ${authenticatedRequest.method}")
                Log.d(TAG, "Заголовки запроса: ${authenticatedRequest.headers}")
                
                var response = chain.proceed(authenticatedRequest)
                
                // Добавить лог с кодом ответа
                Log.d(TAG, "Получен ответ с кодом: ${response.code}")
                
                // If we get a 401 Unauthorized, try to refresh the token
                if (response.code == 401 && !isRefreshing) {
                    Log.d(TAG, "Received 401 response, attempting to refresh token")
                    
                    response.close()
                    isRefreshing = true
                    
                    // Try to refresh the token
                    val refreshToken = AuthManager.getRefreshToken()
                    
                    if (refreshToken != null) {
                        val newToken = refreshToken(refreshToken)
                        
                        if (newToken != null) {
                            // Success - retry with new token
                            AuthManager.saveTokens(newToken, refreshToken)
                            accessToken = newToken
                            
                            // Create a new request with the new token
                            val newAuthenticatedRequest = addAuthHeader(originalRequest, accessToken)
                            isRefreshing = false
                            Log.d(TAG, "Повторяем запрос с новым токеном: ${newAuthenticatedRequest.url}")
                            return chain.proceed(newAuthenticatedRequest)
                        }
                    }
                    
                    isRefreshing = false
                }
                
                return response
            } else {
                Log.d(TAG, "No access token available, proceeding without authentication")
                chain.proceed(originalRequest)
            }
        } catch (e: Exception) { 
            Log.e(TAG, "Error in authentication flow: ${e.message}")
            chain.proceed(originalRequest)
        }
    }
    
    private fun addAuthHeader(request: Request, token: String): Request {
        val newRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        // Добавить лог для проверки формата заголовка авторизации
        Log.d(TAG, "Заголовок авторизации: ${newRequest.header("Authorization")?.take(30)}...")
        return newRequest
    }
    
    private fun refreshToken(refreshToken: String): String? {
        Log.d(TAG, "Attempting to refresh token")
        
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(RetrofitClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonConfig.createGson()))
                .build()
            
            val tempApiService = retrofit.create(ApiService::class.java)
            
            // This has to be a blocking call as the interceptor is not a suspend function
            runBlocking {
                val response = tempApiService.refreshToken(TokenRefreshRequest(refreshToken))
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Token refresh successful")
                    val newToken = response.body()?.access
                    Log.d(TAG, "Новый токен получен: ${newToken?.take(15)}...")
                    newToken
                } else {
                    Log.e(TAG, "Token refresh failed: ${response.code()} - ${response.message()}")
                    null
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception refreshing token: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Exception refreshing token: ${e.message}", e)
            null
        }
    }
}