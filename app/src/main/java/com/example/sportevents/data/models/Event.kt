package com.example.sportevents.data.models

import java.math.BigDecimal

data class Event(
    val id: Int,
    val title: String,
    val description: String,
    val organizer: User,
    val sport_type: SportType,
    val event_type: EventType,
    val location: Location?,
    val custom_location_text: String?,
    val start_datetime: String,
    val end_datetime: String?,
    val registration_deadline: String?,
    val max_participants: Int?,
    val current_participants_count: Int,
    val status: String,
    val is_public: Boolean,
    val entry_fee: BigDecimal?,
    val contact_email: String?,
    val contact_phone: String?,
    val created_at: String,
    val updated_at: String
)

data class EventCreateRequest(
    val title: String,
    val description: String,
    val sport_type_id: Int,
    val event_type_id: Int,
    val location_id: Int?,
    val custom_location_text: String?,
    val start_datetime: String,
    val end_datetime: String?,
    val registration_deadline: String?,
    val max_participants: Int?,
    val status: String,
    val is_public: Boolean,
    val entry_fee: BigDecimal?,
    val contact_email: String?,
    val contact_phone: String?
)
