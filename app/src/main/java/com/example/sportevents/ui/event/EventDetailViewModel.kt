package com.example.sportevents.ui.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.RegistrationRepository
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {
    private val TAG = "EventDetailViewModel"
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()

    private val _event = MutableLiveData<NetworkResult<Event>>()
    val event: LiveData<NetworkResult<Event>> = _event

    private val _registrationResult = MutableLiveData<NetworkResult<EventRegistration>>()
    val registrationResult: LiveData<NetworkResult<EventRegistration>> = _registrationResult

    private val _unregisterResult = MutableLiveData<NetworkResult<Unit>>()
    val unregisterResult: LiveData<NetworkResult<Unit>> = _unregisterResult

    private val _userRegistrations = MutableLiveData<NetworkResult<List<EventRegistration>>>()
    val userRegistrations: LiveData<NetworkResult<List<EventRegistration>>> = _userRegistrations

    fun loadEvent(eventId: Int) {
        _event.value = NetworkResult.Loading

        viewModelScope.launch {
            _event.value = eventRepository.getEvent(eventId)
        }
    }

    fun registerForEvent(eventId: Int, notes: String? = null) {
        if (!AuthManager.isLoggedIn()) {
            _registrationResult.value = NetworkResult.Error("You must be logged in to register")
            return
        }

        _registrationResult.value = NetworkResult.Loading

        viewModelScope.launch {
            _registrationResult.value = registrationRepository.registerForEvent(eventId, notes)
            // Refresh user registrations after registering
            loadUserRegistrations()
        }
    }

    fun unregisterFromEvent(eventId: Int) {
        if (!AuthManager.isLoggedIn()) {
            _unregisterResult.value = NetworkResult.Error("You must be logged in to unregister")
            return
        }

        _unregisterResult.value = NetworkResult.Loading

        viewModelScope.launch {
            _unregisterResult.value = registrationRepository.unregisterFromEvent(eventId)
            // Refresh user registrations after unregistering
            loadUserRegistrations()
        }
    }

    fun loadUserRegistrations() {
        if (!AuthManager.isLoggedIn()) {
            _userRegistrations.value = NetworkResult.Error("User not logged in")
            return
        }

        Log.d(TAG, "Loading user registrations")
        _userRegistrations.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = registrationRepository.getUserRegistrations()
            Log.d(TAG, "User registrations loaded: $result")
            _userRegistrations.value = result
        }
    }

    fun isUserRegisteredForEvent(event: Event?): Boolean {
        if (event == null || !AuthManager.isLoggedIn()) {
            return false
        }

        val registrations = _userRegistrations.value
        if (registrations is NetworkResult.Success) {
            Log.d(TAG, "Checking registrations for event ID: ${event.id}")
            Log.d(TAG, "User has ${registrations.data.size} registrations")
            
            // Find registration for this event
            return registrations.data.any { registration ->
                val regEventId = registration.getEventId()
                Log.d(TAG, "Comparing registration event ID: $regEventId with event ID: ${event.id}")
                regEventId == event.id
            }
        }
        return false
    }

    fun getRegistrationStatusForEvent(event: Event?): String? {
        if (event == null || !AuthManager.isLoggedIn()) {
            return null
        }

        val registrations = _userRegistrations.value
        if (registrations is NetworkResult.Success) {
            // Find registration status for this event
            val registration = registrations.data.find { registration ->
                registration.getEventId() == event.id
            }
            return registration?.status
        }
        return null
    }
}