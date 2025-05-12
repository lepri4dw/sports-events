package com.example.sportevents.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.util.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val repository = EventRepository()

    private val _events = MutableLiveData<Resource<List<Event>>>()
    val events: LiveData<Resource<List<Event>>> = _events

    private val _filteredEvents = MutableLiveData<List<Event>>()
    val filteredEvents: LiveData<List<Event>> = _filteredEvents

    // Store filter parameters
    private var sportTypeId: Int? = null
    private var eventTypeId: Int? = null
    private var searchQuery: String? = null
    private var city: String? = null
    private var dateFrom: String? = null
    private var dateTo: String? = null
    private var includePrivate: Boolean = false

    init {
        loadEvents()
    }

    fun loadEvents() {
        _events.value = Resource.Loading

        viewModelScope.launch {
            val result = repository.getEvents(
                sportTypeId = sportTypeId,
                eventTypeId = eventTypeId,
                search = searchQuery,
                city = city,
                dateFrom = dateFrom,
                dateTo = dateTo,
                includePrivate = includePrivate
            )

            _events.value = result

            if (result is Resource.Success) {
                _filteredEvents.value = result.data
            }
        }
    }

    fun applyFilters(
        sportType: Int? = null,
        eventType: Int? = null,
        search: String? = null,
        city: String? = null,
        from: String? = null,
        to: String? = null,
        includePrivate: Boolean = false
    ) {
        this.sportTypeId = sportType
        this.eventTypeId = eventType
        this.searchQuery = search
        this.city = city
        this.dateFrom = from
        this.dateTo = to
        this.includePrivate = includePrivate

        loadEvents()
    }

    fun clearFilters() {
        sportTypeId = null
        eventTypeId = null
        searchQuery = null
        city = null
        dateFrom = null
        dateTo = null
        includePrivate = false

        loadEvents()
    }
}