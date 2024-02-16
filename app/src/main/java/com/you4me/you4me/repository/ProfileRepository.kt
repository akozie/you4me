package com.you4me.you4me.repository

import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource

class ProfileRepository(private val apiCollector: ApiCollector) : BaseRepository() {

    suspend fun getGenders(): Resource<ArrayList<ValueLabelResponse>> {
        return safeApiCall {
            apiCollector.getGenders()
        }
    }

    suspend fun getSexualOrientations() = safeApiCall {
        apiCollector.getSexualOrientations()
    }

    suspend fun getAgeGroups() = safeApiCall {
        apiCollector.getAgeGroups()
    }

    suspend fun getReligions() = safeApiCall {
        apiCollector.getReligions()
    }

    suspend fun getCountries() = safeApiCall {
        apiCollector.getCountries()
    }

    suspend fun getStates(countryId: String) = safeApiCall {
        apiCollector.getStates(countryId)
    }

}