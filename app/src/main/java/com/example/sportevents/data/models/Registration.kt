package com.example.sportevents.data.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap

data class EventRegistration(
    val id: Int,
    val event: Any?,
    val user: User,
    val registration_datetime: String,
    val status: String,
    val notes_by_user: String?
) {
    private val TAG = "EventRegistration"
    
    fun getEvent(): Event? {
        return when (event) {
            is Event -> event
            is Int -> null
            is Map<*, *> -> null // Обработка JSON объекта
            else -> null
        }
    }
    
    fun getEventId(): Int? {
        return when (event) {
            is Event -> event.id
            is Int -> event
            is Map<*, *> -> {
                try {
                    val map = event as Map<*, *>
                    val idValue = map["id"]
                    Log.d(TAG, "Извлечение ID события из Map: $idValue")
                    when (idValue) {
                        is Int -> idValue
                        is Double -> idValue.toInt()
                        is String -> idValue.toIntOrNull()
                        else -> null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при извлечении ID события: ${e.message}")
                    null
                }
            }
            is LinkedTreeMap<*, *> -> {
                try {
                    val idValue = (event as LinkedTreeMap<*, *>)["id"]
                    Log.d(TAG, "Извлечение ID события из LinkedTreeMap: $idValue")
                    when (idValue) {
                        is Int -> idValue
                        is Double -> idValue.toInt()
                        is String -> idValue.toIntOrNull()
                        else -> null
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при извлечении ID события: ${e.message}")
                    null
                }
            }
            else -> {
                Log.w(TAG, "Неизвестный тип данных события: ${event?.javaClass?.name}")
                null
            }
        }
    }
}

data class RegistrationRequest(
    val notes_by_user: String? = null,
    val user_id: Int? = null
)

data class RegistrationStatusUpdateRequest(
    val status: String
)