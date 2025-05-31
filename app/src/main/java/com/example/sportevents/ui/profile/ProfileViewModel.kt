package com.example.sportevents.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.User
import com.example.sportevents.data.repositories.AuthRepository
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.RegistrationRepository
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch

data class UserStatistics(
    val eventsCreated: Int = 0,
    val eventsJoined: Int = 0
)

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()
    
    private val _userProfile = MutableLiveData<NetworkResult<User>>()
    val userProfile: LiveData<NetworkResult<User>> = _userProfile
    
    private val _userStatistics = MutableLiveData<UserStatistics>()
    val userStatistics: LiveData<UserStatistics> = _userStatistics
    
    // Events for display in the profile tabs
    private val _events = MutableLiveData<NetworkResult<List<Event>>>()
    val events: LiveData<NetworkResult<List<Event>>> = _events
    
    fun loadUserProfile() {
        _userProfile.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()
            _userProfile.value = result
            
            if (result is NetworkResult.Success) {
                // Update the stored user data
                AuthManager.saveUser(result.data)
            }
        }
    }
    
    fun loadUserStatistics() {
        viewModelScope.launch {
            val currentUserId = AuthManager.getCurrentUser()?.id ?: return@launch
            
            // Get events created by user
            val eventsResult = eventRepository.getEvents(includePrivate = true)
            var eventsCreatedCount = 0
            
            if (eventsResult is NetworkResult.Success) {
                eventsCreatedCount = eventsResult.data.count { it.organizer.id == currentUserId }
            }
            
            // Get events joined by user
            val registrationsResult = registrationRepository.getUserRegistrations()
            var eventsJoinedCount = 0
            
            if (registrationsResult is NetworkResult.Success) {
                eventsJoinedCount = registrationsResult.data.size
            }
            
            _userStatistics.value = UserStatistics(
                eventsCreated = eventsCreatedCount,
                eventsJoined = eventsJoinedCount
            )
        }
    }
    
    fun updateUserProfile(updates: Map<String, Any>) {
        _userProfile.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val result = authRepository.updateCurrentUser(updates)
            _userProfile.value = result
            
            if (result is NetworkResult.Success) {
                // Update the stored user data
                AuthManager.saveUser(result.data)
            }
        }
    }
    
    // Functions for tabs in profile screen
    fun loadUserCreatedEvents() {
        _events.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val currentUser = AuthManager.getCurrentUser()
            if (currentUser == null) {
                _events.value = NetworkResult.Error("User not logged in")
                return@launch
            }
            
            val result = eventRepository.getEvents(includePrivate = true)
            if (result is NetworkResult.Success) {
                // Filter events created by the current user
                val filteredEvents = result.data.filter { it.organizer.id == currentUser.id }
                _events.value = NetworkResult.Success(filteredEvents)
            } else if (result is NetworkResult.Error) {
                _events.value = NetworkResult.Error(result.message)
            }
        }
    }
    
    fun loadUserRegisteredEvents() {
        _events.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val registrationsResult = registrationRepository.getUserRegistrations()
            
            if (registrationsResult is NetworkResult.Success) {
                // Extract event IDs
                val eventIds = registrationsResult.data.mapNotNull { it.getEventId() }
                
                if (eventIds.isEmpty()) {
                    _events.value = NetworkResult.Success(emptyList())
                    return@launch
                }
                
                // Fetch full event details for each registered event
                val events = mutableListOf<Event>()
                var hasError = false
                
                for (id in eventIds) {
                    val eventResult = eventRepository.getEvent(id)
                    if (eventResult is NetworkResult.Success) {
                        events.add(eventResult.data)
                    } else {
                        hasError = true
                        break
                    }
                }
                
                if (!hasError) {
                    _events.value = NetworkResult.Success(events)
                } else {
                    _events.value = NetworkResult.Error("Failed to load some registered events")
                }
            } else {
                _events.value = NetworkResult.Error("Failed to load registrations")
            }
        }
    }
} 