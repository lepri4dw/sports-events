package com.example.sportevents.ui.dashboard

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

class DashboardViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()

    private val _events = MutableLiveData<NetworkResult<List<Event>>>()
    val events: LiveData<NetworkResult<List<Event>>> = _events

    private val _registrations = MutableLiveData<NetworkResult<List<EventRegistration>>>()
    val registrations: LiveData<NetworkResult<List<EventRegistration>>> = _registrations

    init {
        loadUserCreatedEvents()
        loadUserRegisteredEvents()
    }

    fun loadUserCreatedEvents() {
        _events.value = NetworkResult.Loading

        viewModelScope.launch {
            // Get events where the current user is the organizer
            val result = eventRepository.getEvents(includePrivate = true)
            
            if (result is NetworkResult.Success) {
                // Filter events where user is organizer
                val currentUserId = AuthManager.getCurrentUser()?.id
                val filteredEvents = result.data.filter { it.organizer.id == currentUserId }
                _events.value = NetworkResult.Success(filteredEvents)
            } else {
                _events.value = result
            }
        }
    }

    fun loadUserRegisteredEvents() {
        _events.value = NetworkResult.Loading
        _registrations.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = registrationRepository.getUserRegistrations()
            
            if (result is NetworkResult.Success) {
                _registrations.value = result
                
                // Extract events from registrations
                val events = result.data.map { it.event }
                _events.value = NetworkResult.Success(events)
            } else {
                _registrations.value = result
                _events.value = NetworkResult.Error((result as NetworkResult.Error).message)
            }
        }
    }

    fun cancelRegistration(eventId: Int) {
        viewModelScope.launch {
            val result = registrationRepository.unregisterFromEvent(eventId)
            if (result is NetworkResult.Success) {
                loadUserRegisteredEvents()
            }
        }
    }
}