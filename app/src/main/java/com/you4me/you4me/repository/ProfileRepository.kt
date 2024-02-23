package com.you4me.you4me.repository

import com.google.gson.JsonObject
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.models.UpdateUserBody
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

    suspend fun updateUserInfo(userId: String, userBody: JsonObject) =
        safeApiCall { apiCollector.updateUserInfo(userId, userBody) }

    suspend fun validateVideoUpload(userId: String) = safeApiCall {
        apiCollector.validateVideoUpload(userId)
    }

    suspend fun registerVideoUpload(
        userId: String,
        registerVideoUploadBody: RegisterVideoUploadBody
    ) = safeApiCall { apiCollector.registerVideoUpload(userId, registerVideoUploadBody) }

    suspend fun updateVideoUrl(videoId : String, obj : JsonObject) =
        safeApiCall { apiCollector.updateVideoUrl(videoId, obj) }
}