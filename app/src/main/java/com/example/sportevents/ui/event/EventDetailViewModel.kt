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
import com.example.sportevents.util.AuthManager
import com.example.sportevents.util.NetworkResult
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {
    private val TAG = "EventDetailViewModel"
    private val eventRepository = EventRepository()
    private val registrationRepository = RegistrationRepository()

    private val _event = MutableLiveData<NetworkResult<Event>>()
    val event: LiveData<NetworkResult<Event>> = _event

    private val _registrationResult = MutableLiveData<NetworkResult<EventRegistration>>()
    val registrationResult: LiveData<NetworkResult<EventRegistration>> = _registrationResult

    private val _unregistrationResult = MutableLiveData<NetworkResult<Void>>()
    val unregistrationResult: LiveData<NetworkResult<Void>> = _unregistrationResult

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
            _registrationResult.value = NetworkResult.Error("Вы должны войти в систему, чтобы зарегистрироваться")
            return
        }

        _registrationResult.value = NetworkResult.Loading

        viewModelScope.launch {
            _registrationResult.value = registrationRepository.registerForEvent(eventId, notes)
            // Обновляем регистрации пользователя после регистрации
            loadUserRegistrations()
        }
    }

    fun unregisterFromEvent(eventId: Int) {
        if (!AuthManager.isLoggedIn()) {
            _unregistrationResult.value = NetworkResult.Error("Вы должны войти в систему, чтобы отменить регистрацию")
            return
        }

        _unregistrationResult.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = registrationRepository.unregisterFromEvent(eventId)
            if (result is NetworkResult.Success) {
                _unregistrationResult.value = result
            } else if (result is NetworkResult.Error) {
                _unregistrationResult.value = NetworkResult.Error(result.message)
            }
            // Обновляем регистрации пользователя после отмены регистрации
            loadUserRegistrations()
        }
    }

    fun loadUserRegistrations() {
        if (!AuthManager.isLoggedIn()) {
            _userRegistrations.value = NetworkResult.Error("Пользователь не вошел в систему")
            return
        }

        Log.d(TAG, "Загрузка регистраций пользователя")
        _userRegistrations.value = NetworkResult.Loading

        viewModelScope.launch {
            val result = registrationRepository.getUserRegistrations()
            
            if (result is NetworkResult.Success) {
                // Сохраняем все регистрации, включая отмененные
                Log.d(TAG, "Регистрации пользователя загружены: ${result.data.size}")
                _userRegistrations.value = result
            } else {
                Log.d(TAG, "Ошибка загрузки регистраций пользователя: $result")
                _userRegistrations.value = result
            }
        }
    }

    fun isUserRegisteredForEvent(event: Event?): Boolean {
        if (event == null || !AuthManager.isLoggedIn()) {
            return false
        }

        val registrations = _userRegistrations.value
        if (registrations is NetworkResult.Success) {
            Log.d(TAG, "Проверка регистраций для мероприятия ID: ${event.id}")
            Log.d(TAG, "У пользователя ${registrations.data.size} регистраций")
            
            // Находим активную регистрацию для этого мероприятия
            return registrations.data.any { registration ->
                val regEventId = registration.getEventId()
                val isActiveStatus = registration.status == "PENDING_APPROVAL" || registration.status == "CONFIRMED"
                Log.d(TAG, "Сравнение ID мероприятия в регистрации: $regEventId с ID мероприятия: ${event.id}, статус: ${registration.status}, активен: $isActiveStatus")
                regEventId == event.id && isActiveStatus
            }
        }
        return false
    }

    fun getRegistrationStatusForEvent(event: Event?): String? {
        if (event == null || !AuthManager.isLoggedIn()) {
            return null
        }

        val registrations = _userRegistrations.value
        if (registrations is NetworkResult.Success) {
            // Находим любую регистрацию для этого мероприятия (включая отмененные)
            val registration = registrations.data.find { registration ->
                registration.getEventId() == event.id
            }
            Log.d(TAG, "Статус регистрации для мероприятия ${event.id}: ${registration?.status}")
            return registration?.status
        }
        return null
    }
    
    fun getRegistrationStatusText(status: String): String {
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