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
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch

class ParticipantListViewModel : ViewModel() {
    private val TAG = "ParticipantListViewModel"
    
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()
    
    private val _event = MutableLiveData<NetworkResult<Event>>()
    val event: LiveData<NetworkResult<Event>> = _event
    
    private val _participants = MutableLiveData<NetworkResult<List<EventRegistration>>>()
    val participants: LiveData<NetworkResult<List<EventRegistration>>> = _participants
    
    private val _statusUpdateResult = MutableLiveData<NetworkResult<EventRegistration>>()
    val statusUpdateResult: LiveData<NetworkResult<EventRegistration>> = _statusUpdateResult
    
    fun loadEvent(eventId: Int) {
        _event.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = eventRepository.getEvent(eventId)
                _event.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки мероприятия: ${e.message}")
                _event.value = NetworkResult.Error("Ошибка загрузки мероприятия: ${e.message}")
            }
        }
    }
    
    fun loadParticipants(eventId: Int) {
        _participants.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = registrationRepository.getEventRegistrations(eventId)
                _participants.value = result
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки участников: ${e.message}")
                _participants.value = NetworkResult.Error("Ошибка загрузки участников: ${e.message}")
            }
        }
    }
    
    fun approveRegistration(registrationId: Int) {
        updateRegistrationStatus(registrationId, "CONFIRMED")
    }
    
    fun rejectRegistration(registrationId: Int) {
        updateRegistrationStatus(registrationId, "REJECTED_BY_ORGANIZER")
    }
    
    private fun updateRegistrationStatus(registrationId: Int, status: String) {
        _statusUpdateResult.value = NetworkResult.Loading
        viewModelScope.launch {
            try {
                val result = registrationRepository.updateRegistrationStatus(registrationId, status)
                _statusUpdateResult.value = result
                
                // Если обновление статуса прошло успешно, обновляем список участников
                if (result is NetworkResult.Success) {
                    // Обновляем текущий список, чтобы не делать запрос к серверу
                    val currentParticipants = _participants.value
                    if (currentParticipants is NetworkResult.Success) {
                        val updatedList = currentParticipants.data.map { registration -> 
                            if (registration.id == registrationId) result.data else registration 
                        }
                        _participants.value = NetworkResult.Success(updatedList)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка обновления статуса регистрации: ${e.message}")
                _statusUpdateResult.value = NetworkResult.Error("Ошибка обновления статуса: ${e.message}")
            }
        }
    }
    
    fun canManageParticipants(eventOwnerId: Int?, currentUserId: Int?): Boolean {
        return eventOwnerId != null && currentUserId != null && eventOwnerId == currentUserId
    }
    
    fun getStatusText(status: String): String {
        return when (status) {
            "PENDING_APPROVAL" -> "Ожидает подтверждения"
            "CONFIRMED" -> "Подтверждено"
            "REJECTED_BY_ORGANIZER" -> "Отклонено организатором"
            "CANCELLED_BY_USER" -> "Отменено пользователем"
            "ATTENDED" -> "Посетил"
            else -> status
        }
    }
} 