package com.example.sportevents.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.models.SportType
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.EventTypeRepository
import com.example.sportevents.data.repositories.SportTypeRepository
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val TAG = "HomeViewModel"
    private val eventRepository = EventRepository()
    private val sportTypeRepository = SportTypeRepository()
    private val eventTypeRepository = EventTypeRepository()

    private val _events = MutableLiveData<NetworkResult<List<Event>>>()
    val events: LiveData<NetworkResult<List<Event>>> = _events

    private val _sportTypes = MutableLiveData<NetworkResult<List<SportType>>>()
    val sportTypes: LiveData<NetworkResult<List<SportType>>> = _sportTypes

    private val _eventTypes = MutableLiveData<NetworkResult<List<EventType>>>()
    val eventTypes: LiveData<NetworkResult<List<EventType>>> = _eventTypes

    // Сообщение об ошибке для отображения пользователю
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // Store filter parameters
    private val selectedSportTypeIds = mutableSetOf<Int>()
    private val selectedEventTypeIds = mutableSetOf<Int>()
    private var searchQuery: String? = null
    private var city: String? = null
    private var dateFrom: String? = null
    private var dateTo: String? = null
    private var status: String? = null

    fun loadEvents() {
        _events.value = NetworkResult.Loading
        Log.d(TAG, "Loading events with filters: sportType=${if (selectedSportTypeIds.isEmpty()) "none" else selectedSportTypeIds}, " +
                "eventType=${if (selectedEventTypeIds.isEmpty()) "none" else selectedEventTypeIds}, " +
                "search=$searchQuery, city=$city, status=$status")

        viewModelScope.launch {
            // Handle the case of multiple filters by doing multiple API requests and merging results
            if (selectedSportTypeIds.size > 1 || selectedEventTypeIds.size > 1) {
                val allEvents = mutableListOf<Event>()
                val processedEventIds = mutableSetOf<Int>()
                var hasError = false
                var errorMessage = ""
                
                // If we have both multiple sport types and event types, we need to do multiple queries
                // and then filter the results
                if (selectedSportTypeIds.isNotEmpty()) {
                    for (sportTypeId in selectedSportTypeIds) {
                        val result = eventRepository.getEvents(
                            sportTypeId = sportTypeId,
                            eventTypeId = if (selectedEventTypeIds.size == 1) selectedEventTypeIds.first() else null,
                            search = searchQuery,
                            city = city,
                            dateFrom = dateFrom,
                            dateTo = dateTo,
                            includePrivate = false
                        )
                        
                        when (result) {
                            is NetworkResult.Success -> {
                                // Add events that haven't been added yet
                                for (event in result.data) {
                                    if (!processedEventIds.contains(event.id) && event.is_public) {
                                        processedEventIds.add(event.id)
                                        allEvents.add(event)
                                    }
                                }
                            }
                            is NetworkResult.Error -> {
                                hasError = true
                                errorMessage = result.message
                            }
                            is NetworkResult.Loading -> {
                                // Ignore
                            }
                        }
                    }
                }
                
                // If we have multiple event types but single/no sport type
                if (selectedEventTypeIds.size > 1 && selectedSportTypeIds.size <= 1) {
                    for (eventTypeId in selectedEventTypeIds) {
                        val result = eventRepository.getEvents(
                            sportTypeId = if (selectedSportTypeIds.size == 1) selectedSportTypeIds.first() else null,
                            eventTypeId = eventTypeId,
                            search = searchQuery,
                            city = city,
                            dateFrom = dateFrom,
                            dateTo = dateTo,
                            includePrivate = false
                        )
                        
                        when (result) {
                            is NetworkResult.Success -> {
                                // Add events that haven't been added yet
                                for (event in result.data) {
                                    if (!processedEventIds.contains(event.id) && event.is_public) {
                                        processedEventIds.add(event.id)
                                        allEvents.add(event)
                                    }
                                }
                            }
                            is NetworkResult.Error -> {
                                hasError = true
                                errorMessage = result.message
                            }
                            is NetworkResult.Loading -> {
                                // Ignore
                            }
                        }
                    }
                }
                
                if (hasError && allEvents.isEmpty()) {
                    Log.e(TAG, "Failed to load events: $errorMessage")
                    _errorMessage.value = "Ошибка загрузки событий: $errorMessage"
                    _events.value = NetworkResult.Error(errorMessage)
                } else {
                    Log.d(TAG, "Successfully loaded ${allEvents.size} events")
                    _events.value = NetworkResult.Success(allEvents)
                }
            } else {
                // Simple case with single sport type and event type filters
                val sportTypeId = if (selectedSportTypeIds.isNotEmpty()) selectedSportTypeIds.first() else null
                val eventTypeId = if (selectedEventTypeIds.isNotEmpty()) selectedEventTypeIds.first() else null
                
                val result = eventRepository.getEvents(
                    sportTypeId = sportTypeId,
                    eventTypeId = eventTypeId,
                    search = searchQuery,
                    city = city,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    includePrivate = false
                )
                
                when (result) {
                    is NetworkResult.Success -> {
                        // Filter out private events
                        val publicEvents = result.data.filter { it.is_public }
                        Log.d(TAG, "Successfully loaded ${publicEvents.size} events")
                        _events.value = NetworkResult.Success(publicEvents)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "Failed to load events: ${result.message}")
                        _errorMessage.value = "Ошибка загрузки событий: ${result.message}"
                        _events.value = result
                    }
                    is NetworkResult.Loading -> {
                        _events.value = result
                    }
                }
            }
        }
    }

    fun loadSportTypes() {
        _sportTypes.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val result = sportTypeRepository.getSportTypes()
            _sportTypes.value = result
            
            if (result is NetworkResult.Error) {
                Log.e(TAG, "Failed to load sport types: ${result.message}")
                _errorMessage.value = "Ошибка загрузки типов спорта: ${result.message}"
            }
        }
    }

    fun loadEventTypes() {
        _eventTypes.value = NetworkResult.Loading
        
        viewModelScope.launch {
            val result = eventTypeRepository.getEventTypes()
            _eventTypes.value = result
            
            if (result is NetworkResult.Error) {
                Log.e(TAG, "Failed to load event types: ${result.message}")
                _errorMessage.value = "Ошибка загрузки типов событий: ${result.message}"
            }
        }
    }

    fun addSportTypeFilter(sportTypeId: Int) {
        selectedSportTypeIds.add(sportTypeId)
    }

    fun removeSportTypeFilter(sportTypeId: Int) {
        selectedSportTypeIds.remove(sportTypeId)
    }

    fun addEventTypeFilter(eventTypeId: Int) {
        selectedEventTypeIds.add(eventTypeId)
    }

    fun removeEventTypeFilter(eventTypeId: Int) {
        selectedEventTypeIds.remove(eventTypeId)
    }

    fun setSearchQuery(query: String?) {
        searchQuery = if (query.isNullOrBlank()) null else query
    }

    fun setCityFilter(cityName: String?) {
        city = if (cityName.isNullOrBlank()) null else cityName
    }

    fun setDateRange(from: String?, to: String?) {
        dateFrom = from
        dateTo = to
    }

    fun setStatusFilter(statusName: String?) {
        status = statusName
    }

    fun applyFilters() {
        loadEvents()
    }

    fun clearFilters() {
        selectedSportTypeIds.clear()
        selectedEventTypeIds.clear()
        searchQuery = null
        city = null
        dateFrom = null
        dateTo = null
        status = null
    }
}