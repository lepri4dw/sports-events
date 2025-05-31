package com.example.sportevents.data.repositories

import android.util.Log
import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.data.models.RegistrationRequest
import com.example.sportevents.data.models.RegistrationStatusUpdateRequest
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult

class RegistrationRepository : BaseRepository() {
    private val TAG = "RegistrationRepository"
    private val apiService = RetrofitClient.apiService

    suspend fun registerForEvent(eventId: Int, notes: String? = null): NetworkResult<EventRegistration> {
        Log.d(TAG, "Регистрация на мероприятие $eventId с заметками: $notes")
        Log.d(TAG, "Токен для запроса регистрации: ${AuthManager.getAccessToken()?.take(15)}...")
        
        val request = RegistrationRequest(notes_by_user = notes)
        return safeApiCall { 
            Log.d(TAG, "Вызов API регистрации для мероприятия $eventId")
            apiService.registerForEvent(eventId, request) 
        }.also { result ->
            when (result) {
                is NetworkResult.Success -> Log.d(TAG, "Successfully registered for event $eventId")
                is NetworkResult.Error -> Log.e(TAG, "Error registering for event $eventId: ${result.message}")
                is NetworkResult.Loading -> Log.d(TAG, "Loading registration for event $eventId")
            }
        }
    }

    suspend fun unregisterFromEvent(eventId: Int): NetworkResult<Unit> {
        Log.d(TAG, "Unregistering from event $eventId")
        Log.d(TAG, "Токен для запроса отмены регистрации: ${AuthManager.getAccessToken()?.take(15)}...")
        
        return safeApiCall { 
            apiService.unregisterFromEvent(eventId) 
        }.also { result ->
            when (result) {
                is NetworkResult.Success -> Log.d(TAG, "Successfully unregistered from event $eventId")
                is NetworkResult.Error -> Log.e(TAG, "Error unregistering from event $eventId: ${result.message}")
                is NetworkResult.Loading -> Log.d(TAG, "Loading unregistration for event $eventId")
            }
        }
    }

    suspend fun getUserRegistrations(): NetworkResult<List<EventRegistration>> {
        Log.d(TAG, "Getting user registrations")
        Log.d(TAG, "Токен для запроса регистраций пользователя: ${AuthManager.getAccessToken()?.take(15)}...")
        
        return safeApiCall { apiService.getUserRegistrations() }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Got ${result.data.results.size} user registrations")
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error getting user registrations: ${result.message}")
                    result
                }
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getEventRegistrations(eventId: Int): NetworkResult<List<EventRegistration>> {
        Log.d(TAG, "Getting registrations for event $eventId")
        Log.d(TAG, "Токен для запроса регистраций мероприятия: ${AuthManager.getAccessToken()?.take(15)}...")
        
        return safeApiCall { apiService.getEventRegistrations(eventId) }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Log.d(TAG, "Got ${result.data.results.size} registrations for event $eventId")
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error getting event registrations: ${result.message}")
                    result
                }
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun updateRegistrationStatus(id: Int, status: String): NetworkResult<EventRegistration> {
        Log.d(TAG, "Updating registration $id status to $status")
        Log.d(TAG, "Токен для запроса обновления статуса: ${AuthManager.getAccessToken()?.take(15)}...")
        
        val request = RegistrationStatusUpdateRequest(status)
        return safeApiCall { 
            apiService.updateRegistrationStatus(id, request) 
        }.also { result ->
            when (result) {
                is NetworkResult.Success -> Log.d(TAG, "Successfully updated registration $id status to $status")
                is NetworkResult.Error -> Log.e(TAG, "Error updating registration status: ${result.message}")
                is NetworkResult.Loading -> Log.d(TAG, "Loading registration status update")
            }
        }
    }
}