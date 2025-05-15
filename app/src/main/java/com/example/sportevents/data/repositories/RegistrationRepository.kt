package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.data.models.RegistrationRequest
import com.example.sportevents.data.models.RegistrationStatusUpdateRequest
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.NetworkResult

class RegistrationRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun registerForEvent(eventId: Int, notes: String? = null): NetworkResult<EventRegistration> {
        val request = RegistrationRequest(notes_by_user = notes)
        return safeApiCall { apiService.registerForEvent(eventId, request) }
    }

    suspend fun unregisterFromEvent(eventId: Int): NetworkResult<Unit> {
        return safeApiCall { apiService.unregisterFromEvent(eventId) }
    }

    suspend fun getUserRegistrations(): NetworkResult<List<EventRegistration>> {
        return safeApiCall { apiService.getUserRegistrations() }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getEventRegistrations(eventId: Int): NetworkResult<List<EventRegistration>> {
        return safeApiCall { apiService.getEventRegistrations(eventId) }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun updateRegistrationStatus(id: Int, status: String): NetworkResult<EventRegistration> {
        val request = RegistrationStatusUpdateRequest(status)
        return safeApiCall { apiService.updateRegistrationStatus(id, request) }
    }
}