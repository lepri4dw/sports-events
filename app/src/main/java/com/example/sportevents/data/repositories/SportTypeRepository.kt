package com.example.sportevents.data.repositories

import com.example.sportevents.data.models.SportType
import com.example.sportevents.data.network.RetrofitClient
import com.example.sportevents.util.Resource

class SportTypeRepository : BaseRepository() {
    private val apiService = RetrofitClient.apiService

    suspend fun getSportTypes(): Resource<List<SportType>> {
        return safeApiCall { apiService.getSportTypes() }
    }

    suspend fun getSportType(id: Int): Resource<SportType> {
        return safeApiCall { apiService.getSportType(id) }
    }
}