package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.Resource

class EventTypeRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getEventTypes(): Resource<List<EventType>> {
        return safeApiCall { apiService.getEventTypes() }
    }

    suspend fun getEventType(id: Int): Resource<EventType> {
        return safeApiCall { apiService.getEventType(id) }
    }
}
