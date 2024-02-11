package com.you4me.you4me.network

import com.google.gson.JsonObject
import com.you4me.you4me.models.LoginResponse
import com.you4me.you4me.models.RegisterResponse
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCollector {

    @POST("login")
    suspend fun login(@Body obj: JsonObject): LoginResponse

    @POST("users")
    suspend fun register(@Body obj: JsonObject): RegisterResponse
}