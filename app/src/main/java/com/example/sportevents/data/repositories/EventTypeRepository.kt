package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.NetworkResult

class EventTypeRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getEventTypes(): NetworkResult<List<EventType>> {
        return safeApiCall { apiService.getEventTypes() }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getEventType(id: Int): NetworkResult<EventType> {
        return safeApiCall { apiService.getEventType(id) }
    }
}
