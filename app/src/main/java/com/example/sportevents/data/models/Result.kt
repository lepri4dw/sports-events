package com.example.sportevents.data.models

data class EventResult(
    val id: Int,
    val event: Event,
    val participant_user: User?,
    val team_name_if_applicable: String?,
    val position: Int?,
    val score: String?,
    val achievement_description: String?,
    val recorded_by_user: User,
    val recorded_at: String
)

data class ResultCreateRequest(
    val participant_user_id: Int?,
    val team_name_if_applicable: String?,
    val position: Int?,
    val score: String?,
    val achievement_description: String?
)