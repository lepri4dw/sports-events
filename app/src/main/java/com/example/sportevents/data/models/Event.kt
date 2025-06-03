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
) {
    // Helper methods
    fun isRegistrationOpen(): Boolean {
        return status == "REGISTRATION_OPEN" || status == "ACTIVE"
    }
    
    fun isFull(): Boolean {
        return max_participants != null && current_participants_count >= max_participants
    }
    
    fun isLocationSpecified(): Boolean {
        return location != null || !custom_location_text.isNullOrBlank()
    }
    
    fun getLocationDisplayText(): String {
        return when {
            location != null -> location.name
            !custom_location_text.isNullOrBlank() -> custom_location_text
            else -> "No location specified"
        }
    }
}

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

data class EventUpdateRequest(
    var title: String? = null,
    var description: String? = null,
    var sport_type_id: Int? = null,
    var event_type_id: Int? = null,
    var location_id: Int? = null,
    var custom_location_text: String? = null,
    var start_datetime: String? = null,
    var end_datetime: String? = null,
    var registration_deadline: String? = null,
    var max_participants: Int? = null,
    var status: String? = null,
    var is_public: Boolean? = null,
    var entry_fee: String? = null,
    var contact_email: String? = null,
    var contact_phone: String? = null
)
