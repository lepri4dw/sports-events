package com.example.sportevents.data.models

data class User(
    val id: Int,
    val email: String,
    val display_name: String,
    val is_active: Boolean,
    val is_staff: Boolean,
    val created_at: String,
    val updated_at: String
)

data class AuthResponse(
    val user: User,
    val access: String,
    val refresh: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val display_name: String,
    val password: String
)

data class TokenRefreshRequest(
    val refresh: String
)

data class TokenRefreshResponse(
    val access: String
)