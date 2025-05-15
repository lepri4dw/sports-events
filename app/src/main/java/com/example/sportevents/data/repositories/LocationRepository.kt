package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.Location
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.NetworkResult

class LocationRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getLocations(city: String? = null): NetworkResult<List<Location>> {
        return safeApiCall { apiService.getLocations(city) }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    NetworkResult.Success(result.data.results)
                }
                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }

    suspend fun getLocation(id: Int): NetworkResult<Location> {
        return safeApiCall { apiService.getLocation(id) }
    }

    suspend fun createLocation(location: Location): NetworkResult<Location> {
        return safeApiCall { apiService.createLocation(location) }
    }
}
