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
import com.example.sportevents.util.Resource
import kotlinx.coroutines.launch
import java.math.BigDecimal

class EventCreateViewModel : ViewModel() {
    private val eventRepository = EventRepository()
    private val sportTypeRepository = SportTypeRepository()
    private val eventTypeRepository = EventTypeRepository()
    private val locationRepository = LocationRepository()

    private val _createEventResult = MutableLiveData<Resource<Event>>()
    val createEventResult: LiveData<Resource<Event>> = _createEventResult

    private val _sportTypes = MutableLiveData<Resource<List<SportType>>>()
    val sportTypes: LiveData<Resource<List<SportType>>> = _sportTypes

    private val _eventTypes = MutableLiveData<Resource<List<EventType>>>()
    val eventTypes: LiveData<Resource<List<EventType>>> = _eventTypes

    private val _locations = MutableLiveData<Resource<List<Location>>>()
    val locations: LiveData<Resource<List<Location>>> = _locations

    init {
        loadSportTypes()
        loadEventTypes()
        loadLocations()
    }

    private fun loadSportTypes() {
        viewModelScope.launch {
            _sportTypes.value = Resource.Loading
            _sportTypes.value = sportTypeRepository.getSportTypes()
        }
    }

    private fun loadEventTypes() {
        viewModelScope.launch {
            _eventTypes.value = Resource.Loading
            _eventTypes.value = eventTypeRepository.getEventTypes()
        }
    }

    private fun loadLocations() {
        viewModelScope.launch {
            _locations.value = Resource.Loading
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
        status: String,
        isPublic: Boolean,
        entryFee: String?,
        contactEmail: String?,
        contactPhone: String?
    ) {
        _createEventResult.value = Resource.Loading

        val entryFeeDecimal = if (entryFee.isNullOrEmpty()) null else BigDecimal(entryFee)

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
            status = status,
            is_public = isPublic,
            entry_fee = entryFeeDecimal,
            contact_email = contactEmail,
            contact_phone = contactPhone
        )

        viewModelScope.launch {
            _createEventResult.value = eventRepository.createEvent(request)
        }
    }
}