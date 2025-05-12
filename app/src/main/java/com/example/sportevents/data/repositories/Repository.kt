package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

open class BaseRepository {
    protected suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                if (response.isSuccessful) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Network error: ${e.message}")
            }
        }
    }
}
