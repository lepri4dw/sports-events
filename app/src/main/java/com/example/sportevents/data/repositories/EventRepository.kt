package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.NetworkResult

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
    ): NetworkResult<List<Event>> {
        return safeApiCall {
            apiService.getEvents(
                sportTypeId, eventTypeId, status, isPublic, search,
                ordering, includePrivate, city, dateFrom, dateTo
            )
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    // Извлекаем список результатов из пагинированного ответа
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> {
                    result
                }
                is NetworkResult.Loading -> {
                    result
                }
            }
        }
    }

    suspend fun getEvent(id: Int): NetworkResult<Event> {
        return safeApiCall { apiService.getEvent(id) }
    }

    suspend fun createEvent(eventCreateRequest: EventCreateRequest): NetworkResult<Event> {
        return safeApiCall { apiService.createEvent(eventCreateRequest) }
    }

    suspend fun updateEvent(id: Int, updates: EventUpdateRequest): NetworkResult<Event> {
        return safeApiCall { apiService.updateEvent(id, updates) }
    }

    suspend fun deleteEvent(id: Int): NetworkResult<Void> {
        return safeApiCallVoid { apiService.deleteEvent(id) }
    }
}