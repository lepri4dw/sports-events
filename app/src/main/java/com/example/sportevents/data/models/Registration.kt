package com.example.sportevents.data.models

import android.util.Log
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap

data class EventRegistration(
    val id: Int,
    val event: Any,
    val user: User,
    @SerializedName("registration_datetime")
    val registrationDatetime: String,
    val status: String,
    @SerializedName("notes_by_user")
    val notesByUser: String?
) {
    private val TAG = "EventRegistration"
    
    fun getEvent(): Event? {
        return when (event) {
            is Event -> event
            is Int -> null
            is Double -> null // Обработка числового ID в JSON
            is Map<*, *> -> try {
                // Преобразование Map в Event
                val map = event as Map<*, *>
                Log.d(TAG, "Извлечение события из Map: $map")
                
                // Такое преобразование требует больше работы и фактически 
                // лучше это делать через Gson с правильным TypeAdapter
                // Это упрощенная версия для примера
                null
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при преобразовании Map в Event: ${e.message}")
                null
            }
            is LinkedTreeMap<*, *> -> try {
                // Аналогично с LinkedTreeMap
                null
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при преобразовании LinkedTreeMap в Event: ${e.message}")
                null
            }
            else -> null
        }
    }
    
    fun getEventId(): Int? {
        if (event == null) return null
        
        return when (event) {
            is Event -> event.id
            is Int -> event
            is Double -> event.toInt() // JSON числа часто парсятся как Double
            is Map<*, *> -> {
                try {
                    val map = event as Map<*, *>
                    val idValue = map["id"]
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
                try {
                    Log.w(TAG, "Неизвестный тип данных события: ${event.javaClass.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при получении информации о типе события: ${e.message}")
                }
                null
            }
        }
    }
}

data class RegistrationRequest(
    @SerializedName("notes_by_user")
    val notesByUser: String? = null,
    @SerializedName("user_id")
    val userId: Int? = null
)

data class RegistrationStatusUpdateRequest(
    val status: String
)