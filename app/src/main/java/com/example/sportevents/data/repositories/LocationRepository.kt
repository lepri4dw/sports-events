package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.Location
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.Resource

class LocationRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getLocations(city: String? = null): Resource<List<Location>> {
        return safeApiCall { apiService.getLocations(city) }
    }

    suspend fun getLocation(id: Int): Resource<Location> {
        return safeApiCall { apiService.getLocation(id) }
    }

    suspend fun createLocation(location: Location): Resource<Location> {
        return safeApiCall { apiService.createLocation(location) }
    }
}
