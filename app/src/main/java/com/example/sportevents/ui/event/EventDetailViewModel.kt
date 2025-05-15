package com.example.sportevents.ui.event

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
        }
    }

    fun unregisterFromEvent(eventId: Int) {
        _unregisterResult.value = NetworkResult.Loading

        viewModelScope.launch {
            _unregisterResult.value = registrationRepository.unregisterFromEvent(eventId)
        }
    }

    fun getUserRegistrations() {
        _userRegistrations.value = NetworkResult.Loading

        viewModelScope.launch {
            _userRegistrations.value = registrationRepository.getUserRegistrations()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return AuthManager.isLoggedIn()
    }

    fun isUserRegisteredForEvent(eventId: Int): Boolean {
        val registrations = _userRegistrations.value
        if (registrations is NetworkResult.Success) {
            return registrations.data.any { it.event.id == eventId && it.status != "CANCELLED_BY_USER" }
        }
        return false
    }
}