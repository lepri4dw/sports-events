package com.example.sportevents.data.models

data class Location(
    val id: Int,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
    val details: String?,
    val created_by_user: Int?
)