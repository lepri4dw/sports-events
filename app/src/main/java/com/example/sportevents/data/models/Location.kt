package com.example.sportevents.data.models

import com.google.gson.annotations.SerializedName

data class Location(
    val id: Int,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
    val details: String?,
    // Поле должно быть nullable и вместо Int использовать Any для совместимости
    // с разными типами возвращаемых данных (Int или User)
    val created_by_user: Any? = null
)