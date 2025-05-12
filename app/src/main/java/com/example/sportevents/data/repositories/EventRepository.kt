package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.Resource

class EventRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getEvents(
        sportTypeId: Int? = null,
        eventTypeId: Int? = null,
        status: String? = null,
        isPublic: Boolean? = null,
        search: String? = null,
        ordering: String? = null,
        includePrivate: Boolean? = null,
        city: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Resource<List<Event>> {
        return safeApiCall {
            apiService.getEvents(
                sportTypeId, eventTypeId, status, isPublic, search,
                ordering, includePrivate, city, dateFrom, dateTo
            )
        }
    }

    suspend fun getEvent(id: Int): Resource<Event> {
        return safeApiCall { apiService.getEvent(id) }
    }

    suspend fun createEvent(eventCreateRequest: EventCreateRequest): Resource<Event> {
        return safeApiCall { apiService.createEvent(eventCreateRequest) }
    }

    suspend fun updateEvent(id: Int, updates: Map<String, Any>): Resource<Event> {
        return safeApiCall { apiService.updateEvent(id, updates) }
    }

    suspend fun deleteEvent(id: Int): Resource<Unit> {
        return safeApiCall { apiService.deleteEvent(id) }
    }
}