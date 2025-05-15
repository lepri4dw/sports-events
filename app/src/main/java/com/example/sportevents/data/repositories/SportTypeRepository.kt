package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.SportType
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.NetworkResult

class SportTypeRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getSportTypes(): NetworkResult<List<SportType>> {
        return safeApiCall { apiService.getSportTypes() }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getSportType(id: Int): NetworkResult<SportType> {
        return safeApiCall { apiService.getSportType(id) }
    }
}