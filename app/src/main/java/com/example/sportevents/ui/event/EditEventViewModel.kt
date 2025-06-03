package com.example.sportevents.ui.event

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportevents.data.models.Event
import com.example.sportevents.data.models.EventType
import com.example.sportevents.data.models.EventUpdateRequest
import com.example.sportevents.data.models.SportType
import com.example.sportevents.data.repositories.EventRepository
import com.example.sportevents.data.repositories.EventTypeRepository
import com.example.sportevents.data.repositories.SportTypeRepository
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditEventViewModel : ViewModel() {
    private val TAG = "EditEventViewModel"
    
    private val eventRepository = EventRepository()
    private val sportTypeRepository = SportTypeRepository()
    private val eventTypeRepository = EventTypeRepository()
    
    private val _event = MutableLiveData<NetworkResult<Event>>()
    val event: LiveData<NetworkResult<Event>> = _event
    
    private val _sportTypes = MutableLiveData<NetworkResult<List<SportType>>>()
    val sportTypes: LiveData<NetworkResult<List<SportType>>> = _sportTypes
    
    private val _eventTypes = MutableLiveData<NetworkResult<List<EventType>>>()
    val eventTypes: LiveData<NetworkResult<List<EventType>>> = _eventTypes
    
    private val _updateResult = MutableLiveData<NetworkResult<Event>>()
    val updateResult: LiveData<NetworkResult<Event>> = _updateResult
    
    fun loadEvent(eventId: Int) {
        _event.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = eventRepository.getEvent(eventId)
                _event.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event: ${e.message}")
                _event.value = NetworkResult.Error("Ошибка загрузки мероприятия: ${e.message}")
            }
        }
    }
    
    fun loadSportTypes() {
        _sportTypes.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = sportTypeRepository.getSportTypes()
                _sportTypes.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading sport types: ${e.message}")
                _sportTypes.value = NetworkResult.Error("Ошибка загрузки типов спорта: ${e.message}")
            }
        }
    }
    
    fun loadEventTypes() {
        _eventTypes.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = eventTypeRepository.getEventTypes()
                _eventTypes.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event types: ${e.message}")
                _eventTypes.value = NetworkResult.Error("Ошибка загрузки типов мероприятий: ${e.message}")
            }
        }
    }
    
    fun updateEvent(
        eventId: Int,
        title: String,
        description: String,
        sportTypeId: Int,
        eventTypeId: Int,
        location: String?,
        startDateTime: String,
        endDateTime: String?,
        registrationDeadline: String?,
        maxParticipants: Int?,
        status: String,
        isPublic: Boolean,
        entryFee: BigDecimal?,
        contactEmail: String?,
        contactPhone: String?
    ) {
        _updateResult.value = NetworkResult.Loading
        
        // Проверяем, является ли местоположение ID или пользовательским текстом
        val locationId = location?.toIntOrNull()
        val customLocationText = if (locationId == null && !location.isNullOrBlank()) location else null
        
        // Создаем объект запроса на обновление со всеми полями
        val updateRequest = EventUpdateRequest(
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
            entry_fee = entryFee?.toString(),
            contact_email = contactEmail,
            contact_phone = contactPhone
        )
        
        Log.d(TAG, "Updating event $eventId with: $updateRequest")
        
        viewModelScope.launch {
            try {
                val result = eventRepository.updateEvent(eventId, updateRequest)
                _updateResult.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Error updating event: ${e.message}")
                _updateResult.value = NetworkResult.Error("Ошибка обновления мероприятия: ${e.message}")
            }
        }
    }
    
    fun formatDateForDisplay(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            
            val date = inputFormat.parse(dateString)
            return outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date: $dateString, ${e.message}")
            return dateString
        }
    }
    
    fun formatDateForApi(dateString: String?): String? {
        if (dateString.isNullOrBlank()) return null
        
        try {
            val inputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            
            val date = inputFormat.parse(dateString)
            return outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting date for API: $dateString, ${e.message}")
            return null
        }
    }
    
    fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }
} 