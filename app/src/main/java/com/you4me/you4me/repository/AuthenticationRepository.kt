package com.you4me.you4me.repository

import com.google.gson.JsonObject
import com.you4me.you4me.models.User
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource

class AuthenticationRepository(private val api: ApiCollector) : BaseRepository() {

    suspend fun login(obj: JsonObject): Resource<User> {
        return safeApiCall {
            api.login(obj)
        }
    }

    suspend fun register(obj: JsonObject): Resource<RegisterResponse> {
        return safeApiCall {
            api.register(obj)
        }
    }

    suspend fun googleSignIn(token: JsonObject) = safeApiCall { api.googleSignIn(token) }

    suspend fun getUser(userId: String) = safeApiCall { api.getUser(userId) }

}