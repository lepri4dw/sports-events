package com.example.sportevents.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventCreateRequest
import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.models.Location
import com.example.sportevents.data.models.SportType
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.EventTypeRepository
import com.example.sportevents.data.repositories.LocationRepository
import com.example.sportevents.data.repositories.SportTypeRepository
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch
import java.math.BigDecimal

class EventCreateViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val sportTypeRepository = SportTypeRepository()
    private val eventTypeRepository = EventTypeRepository()
    private val locationRepository = LocationRepository()

    private val _createEventResult = MutableLiveData<NetworkResult<Event>>()
    val createEventResult: LiveData<NetworkResult<Event>> = _createEventResult

    private val _sportTypes = MutableLiveData<NetworkResult<List<SportType>>>()
    val sportTypes: LiveData<NetworkResult<List<SportType>>> = _sportTypes

    private val _eventTypes = MutableLiveData<NetworkResult<List<EventType>>>()
    val eventTypes: LiveData<NetworkResult<List<EventType>>> = _eventTypes

    private val _locations = MutableLiveData<NetworkResult<List<Location>>>()
    val locations: LiveData<NetworkResult<List<Location>>> = _locations

    init {
        loadSportTypes()
        loadEventTypes()
        loadLocations()
    }

    fun loadSportTypes() {
        viewModelScope.launch {
            _sportTypes.value = NetworkResult.Loading
            _sportTypes.value = sportTypeRepository.getSportTypes()
        }
    }

    fun loadEventTypes() {
        viewModelScope.launch {
            _eventTypes.value = NetworkResult.Loading
            _eventTypes.value = eventTypeRepository.getEventTypes()
        }
    }

    fun loadLocations() {
        viewModelScope.launch {
            _locations.value = NetworkResult.Loading
            _locations.value = locationRepository.getLocations()
        }
    }

    fun createEvent(
        title: String,
        description: String,
        sportTypeId: Int,
        eventTypeId: Int,
        locationId: Int?,
        customLocationText: String?,
        startDateTime: String,
        endDateTime: String?,
        registrationDeadline: String?,
        maxParticipants: Int?,
        isPublic: Boolean,
        entryFee: BigDecimal?,
        contactEmail: String?,
        contactPhone: String?
    ) {
        _createEventResult.value = NetworkResult.Loading

        val request = EventCreateRequest(
            title = title,
            description = description,
            sport_type_id = sportTypeId,
            event_type_id = eventTypeId,
            location_id = locationId,
            custom_location_text = customLocationText,
            start_datetime = startDateTime,
            end_datetime = endDateTime,
            registration_deadline = registrationDeadline,
            max_participants = maxParticipants,
            status = "ACTIVE",
            is_public = isPublic,
            entry_fee = entryFee,
            contact_email = contactEmail,
            contact_phone = contactPhone
        )

        viewModelScope.launch {
            _createEventResult.value = eventRepository.createEvent(request)
        }
    }
}