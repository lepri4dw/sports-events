package com.example.sportevents.data.models

data class EventRegistration(
    val id: Int,
    val event: Event,
    val user: User,
    val registration_datetime: String,
    val status: String,
    val notes_by_user: String?
)

data class RegistrationRequest(
    val notes_by_user: String? = null,
    val user_id: Int? = null
)

data class RegistrationStatusUpdateRequest(
    val status: String
)