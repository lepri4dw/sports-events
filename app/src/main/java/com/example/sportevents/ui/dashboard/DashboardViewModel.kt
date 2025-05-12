package com.example.sportevents.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventRegistration
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.RegistrationRepository
import com.example.sportevents.util.Resource
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()

    private val _userEvents = MutableLiveData<Resource<List<Event>>>()
    val userEvents: LiveData<Resource<List<Event>>> = _userEvents

    private val _userRegistrations = MutableLiveData<Resource<List<EventRegistration>>>()
    val userRegistrations: LiveData<Resource<List<EventRegistration>>> = _userRegistrations

    init {
        loadUserEvents()
        loadUserRegistrations()
    }

    fun loadUserEvents() {
        _userEvents.value = Resource.Loading

        viewModelScope.launch {
            // Get events where the current user is the organizer
            // This assumes the API handles this filtering
            val result = eventRepository.getEvents(includePrivate = true)
            if (result is Resource.Success) {
                // Filter events where user is organizer
                // This is a client-side fallback if API doesn't filter by organizer
                val currentUserId = com.example.sportevents.util.AuthManager.getCurrentUser()?.id
                val filteredEvents = result.data.filter { it.organizer.id == currentUserId }
                _userEvents.value = Resource.Success(filteredEvents)
            } else {
                _userEvents.value = result
            }
        }
    }

    fun loadUserRegistrations() {
        _userRegistrations.value = Resource.Loading

        viewModelScope.launch {
            _userRegistrations.value = registrationRepository.getUserRegistrations()
        }
    }

    fun cancelRegistration(eventId: Int) {
        viewModelScope.launch {
            val result = registrationRepository.unregisterFromEvent(eventId)
            if (result is Resource.Success) {
                loadUserRegistrations()
            }
        }
    }
}