package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.*
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.Resource

class RegistrationRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun registerForEvent(eventId: Int, notes: String? = null): Resource<EventRegistration> {
        val request = RegistrationRequest(notes_by_user = notes)
        return safeApiCall { apiService.registerForEvent(eventId, request) }
    }

    suspend fun unregisterFromEvent(eventId: Int): Resource<Unit> {
        return safeApiCall { apiService.unregisterFromEvent(eventId) }
    }

    suspend fun getUserRegistrations(): Resource<List<EventRegistration>> {
        return safeApiCall { apiService.getUserRegistrations() }
    }

    suspend fun getEventRegistrations(eventId: Int): Resource<List<EventRegistration>> {
        return safeApiCall { apiService.getEventRegistrations(eventId) }
    }

    suspend fun updateRegistrationStatus(id: Int, status: String): Resource<EventRegistration> {
        val request = RegistrationStatusUpdateRequest(status)
        return safeApiCall { apiService.updateRegistrationStatus(id, request) }
    }
}