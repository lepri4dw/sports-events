package com.example.sportevents.data.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object GsonConfig {
    
    /**
     * Создаем экземпляр Gson с пользовательскими настройками для типов
     */
    fun createGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(Any::class.java, FlexibleTypeAdapter())
            .create()
    }
    
    /**
     * Адаптер для гибкой десериализации типов
     * Позволяет обрабатывать JSON объекты и примитивы как Any
     */
    class FlexibleTypeAdapter : JsonDeserializer<Any> {
        override fun deserialize(
            json: JsonElement, 
            typeOfT: Type, 
            context: JsonDeserializationContext
        ): Any? {
            return when {
                json.isJsonNull -> null
                json.isJsonPrimitive -> {
                    val primitive = json.asJsonPrimitive
                    when {
                        primitive.isNumber -> {
                            try {
                                primitive.asInt
                            } catch (e: Exception) {
                                try {
                                    primitive.asDouble
                                } catch (e2: Exception) {
                                    primitive.asString
                                }
                            }
                        }
                        primitive.isBoolean -> primitive.asBoolean
                        else -> primitive.asString
                    }
                }
                json.isJsonObject -> json.asJsonObject
                json.isJsonArray -> json.asJsonArray
                else -> null
            }
        }
    }
} 