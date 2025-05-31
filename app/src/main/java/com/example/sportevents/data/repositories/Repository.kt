package com.example.sportevents.data.repositories

import android.util.Log
import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseRepository {
    private val TAG = "BaseRepository"
    
    protected suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                Log.d(TAG, "API запрос выполнен, код ответа: ${response.code()}")
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Получен успешный ответ с телом")
                        NetworkResult.Success(body)
                    } else {
                        Log.e(TAG, "Response body is null")
                        NetworkResult.Error("Response body is null")
                    }
                } else {
                    val code = response.code()
                    val errorMessage = "API error: $code - ${response.message()}"
                    Log.e(TAG, errorMessage)
                    
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        
                        // Special handling for 401 errors
                        if (code == 401) {
                            Log.e(TAG, "Received 401 Unauthorized - token may be invalid or expired")
                            Log.e(TAG, "Текущий токен: ${AuthManager.getAccessToken()?.take(15)}...")
                            
                            // Check if we're currently logged in according to AuthManager
                            val isLoggedIn = AuthManager.isLoggedIn()
                            Log.d(TAG, "Current login status according to AuthManager: $isLoggedIn")
                            
                            if (isLoggedIn) {
                                // We think we're logged in but server says no - consider refreshing token
                                // or forcing a re-login at the UI level
                                Log.d(TAG, "User thinks they're logged in but server disagrees")
                                return@withContext NetworkResult.Error("Authentication error - please log in again")
                            } else {
                                return@withContext NetworkResult.Error("Authentication required - please log in")
                            }
                        }
                        
                        // Try to extract more specific error message from JSON response
                        val detailMessage = try {
                            val jsonObj = JSONObject(errorBody ?: "{}")
                            if (jsonObj.has("detail")) {
                                val detail = jsonObj.getString("detail")
                                Log.e(TAG, "Детальное сообщение об ошибке: $detail")
                                detail
                            } else {
                                errorBody
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Не удалось разобрать JSON ошибки: ${e.message}")
                            errorBody
                        }
                        
                        NetworkResult.Error(detailMessage ?: errorMessage)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing error response", e)
                        NetworkResult.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out"
                    is UnknownHostException -> "Unable to connect to server"
                    is JsonParseException -> "Error parsing response: ${e.message}"
                    else -> "Network error: ${e.javaClass.simpleName} - ${e.message}"
                }
                Log.e(TAG, errorMessage, e)
                Log.e(TAG, "Ошибка сети при текущем токене: ${AuthManager.getAccessToken()?.take(15)}...")
                NetworkResult.Error(errorMessage)
            }
        }
    }
}
