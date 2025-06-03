package com.example.sportevents.data.repositories

import android.util.Log
import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import com.example.sportevents.util.createVoidSuccess
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseRepository {
    private val TAG = "BaseRepository"
    
    // Специальный метод для обработки 204 No Content для Unit ответов
    protected suspend fun safeApiCallUnit(call: suspend () -> Response<Unit>): NetworkResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                Log.d(TAG, "API запрос выполнен, код ответа: ${response.code()}")
                
                if (response.isSuccessful) {
                    // Специальная обработка для 204 No Content
                    if (response.code() == 204) {
                        Log.d(TAG, "Получен успешный ответ 204 No Content")
                        return@withContext NetworkResult.Success(Unit)
                    }
                    
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Получен успешный ответ с телом")
                        NetworkResult.Success(body)
                    } else {
                        // Для Unit типа можно вернуть Unit даже если тело null
                        Log.d(TAG, "Тело ответа null, но тип Unit, возвращаем успех")
                        NetworkResult.Success(Unit)
                    }
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    // Специальный метод для обработки 204 No Content для Void ответов
    protected suspend fun safeApiCallVoid(call: suspend () -> Response<Void>): NetworkResult<Void> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                Log.d(TAG, "API запрос выполнен, код ответа: ${response.code()}")
                
                if (response.isSuccessful) {
                    // Для Void типа всегда возвращаем специальный Success с null
                    Log.d(TAG, "Получен успешный ответ ${response.code()}, возвращаем успешный Void результат")
                    return@withContext createVoidSuccess()
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    // Основной метод для всех остальных типов ответов
    protected suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                Log.d(TAG, "API запрос выполнен, код ответа: ${response.code()}")
                
                if (response.isSuccessful) {
                    // Для других типов 204 No Content является ошибкой, т.к. нет тела
                    if (response.code() == 204) {
                        Log.e(TAG, "204 No Content получен, но ожидалось тело ответа")
                        return@withContext NetworkResult.Error("Response code 204 received but expected a body")
                    }
                    
                    val body = response.body()
                    if (body != null) {
                        Log.d(TAG, "Получен успешный ответ с телом")
                        NetworkResult.Success(body)
                    } else {
                        Log.e(TAG, "Response body is null")
                        NetworkResult.Error("Response body is null")
                    }
                } else {
                    handleErrorResponse(response)
                }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
    
    // Вынесли обработку ошибок в отдельный метод
    private suspend fun <T> handleErrorResponse(response: Response<T>): NetworkResult<T> {
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
                    return NetworkResult.Error("Authentication error - please log in again")
                } else {
                    return NetworkResult.Error("Authentication required - please log in")
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
            
            return NetworkResult.Error(detailMessage ?: errorMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing error response", e)
            return NetworkResult.Error(errorMessage)
        }
    }
    
    // Обработка исключений в отдельном методе
    private fun <T> handleException(e: Exception): NetworkResult<T> {
        val errorMessage = when (e) {
            is SocketTimeoutException -> "Connection timed out"
            is UnknownHostException -> "Unable to connect to server"
            is JsonParseException -> "Error parsing response: ${e.message}"
            else -> "Network error: ${e.javaClass.simpleName} - ${e.message}"
        }
        Log.e(TAG, errorMessage, e)
        Log.e(TAG, "Ошибка сети при текущем токене: ${AuthManager.getAccessToken()?.take(15)}...")
        return NetworkResult.Error(errorMessage)
    }
}
