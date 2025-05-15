package com.example.sportevents.data.repositories

import android.util.Log
import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

open class BaseRepository {
    private val TAG = "BaseRepository"
    
    protected suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        NetworkResult.Success(body)
                    } else {
                        Log.e(TAG, "Response body is null")
                        NetworkResult.Error("Response body is null")
                    }
                } else {
                    val errorMessage = "API error: ${response.code()} - ${response.message()}"
                    Log.e(TAG, errorMessage)
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error body: $errorBody")
                        NetworkResult.Error("$errorMessage: $errorBody")
                    } catch (e: Exception) {
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
                NetworkResult.Error(errorMessage)
            }
        }
    }
}
