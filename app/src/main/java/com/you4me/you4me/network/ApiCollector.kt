package com.you4me.you4me.network

import com.you4me.you4me.models.LoginResponse
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiCollector {

    @POST("")
    suspend fun login(@Body obj : JSONObject) : LoginResponse
}