package com.example.sportevents.data.network

import com.example.sportevents.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("users/register/")
    suspend fun registerUser(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("users/login/")
    suspend fun loginUser(@Body request: LoginRequest): Response<AuthResponse>

    @POST("token/refresh/")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): Response<TokenRefreshResponse>

    @GET("users/me/")
    suspend fun getCurrentUser(): Response<User>

    @PUT("users/me/")
    suspend fun updateCurrentUser(@Body updates: Map<String, Any>): Response<User>

    // Sport Types
    @GET("sport-types/")
    suspend fun getSportTypes(): Response<PaginatedResponse<SportType>>

    @GET("sport-types/{id}/")
    suspend fun getSportType(@Path("id") id: Int): Response<SportType>

    // Event Types
    @GET("event-types/")
    suspend fun getEventTypes(): Response<PaginatedResponse<EventType>>

    @GET("event-types/{id}/")
    suspend fun getEventType(@Path("id") id: Int): Response<EventType>

    // Locations
    @GET("locations/")
    suspend fun getLocations(@Query("city") city: String? = null): Response<PaginatedResponse<Location>>

    @POST("locations/")
    suspend fun createLocation(@Body location: Location): Response<Location>

    @GET("locations/{id}/")
    suspend fun getLocation(@Path("id") id: Int): Response<Location>

    // Events
    @GET("events/")
    suspend fun getEvents(
        @Query("sport_type") sportTypeId: Int? = null,
        @Query("event_type") eventTypeId: Int? = null,
        @Query("status") status: String? = null,
        @Query("is_public") isPublic: Boolean? = null,
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String? = null,
        @Query("include_private") includePrivate: Boolean? = null,
        @Query("city") city: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<PaginatedResponse<Event>>

    @POST("events/")
    suspend fun createEvent(@Body eventCreateRequest: EventCreateRequest): Response<Event>

    @GET("events/{id}/")
    suspend fun getEvent(@Path("id") id: Int): Response<Event>

    @PUT("events/{id}/")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body updates: EventUpdateRequest
    ): Response<Event>

    @DELETE("events/{id}/")
    suspend fun deleteEvent(@Path("id") id: Int): Response<Void>

    // Event Registrations
    @POST("events/{eventId}/register/")
    suspend fun registerForEvent(
        @Path("eventId") eventId: Int,
        @Body request: RegistrationRequest
    ): Response<EventRegistration>

    @DELETE("events/{eventId}/unregister/")
    suspend fun unregisterFromEvent(@Path("eventId") eventId: Int): Response<Void>

    @GET("events/{eventId}/registrations/")
    suspend fun getEventRegistrations(@Path("eventId") eventId: Int): Response<List<EventRegistration>>

    @GET("registrations/")
    suspend fun getUserRegistrations(): Response<PaginatedResponse<EventRegistration>>

    @GET("registrations/{id}/")
    suspend fun getRegistration(@Path("id") id: Int): Response<EventRegistration>

    @PUT("registrations/{id}/status/")
    suspend fun updateRegistrationStatus(
        @Path("id") id: Int,
        @Body request: RegistrationStatusUpdateRequest
    ): Response<EventRegistration>

    // Results
    @POST("events/{eventId}/add_result/")
    suspend fun addEventResult(
        @Path("eventId") eventId: Int,
        @Body request: ResultCreateRequest
    ): Response<EventResult>

    @GET("results/")
    suspend fun getResults(@Query("event_id") eventId: Int? = null): Response<PaginatedResponse<EventResult>>
}
