package com.you4me.you4me.repository

import com.google.gson.JsonObject
import com.you4me.you4me.models.LoginResponse
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import org.json.JSONObject

class AuthenticationRepository(private val api: ApiCollector) : BaseRepository() {

    suspend fun login(obj: JsonObject): Resource<LoginResponse> {
        return safeApiCall {
            api.login(obj)
        }
    }

    suspend fun register(obj: JsonObject): Resource<RegisterResponse> {
        return safeApiCall {
            api.register(obj)
        }
    }
}