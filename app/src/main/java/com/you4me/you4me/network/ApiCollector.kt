package com.you4me.you4me.network

import com.google.gson.JsonObject
import com.you4me.you4me.models.LoginResponse
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.models.ValueLabelResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiCollector {

    @POST("login")
    suspend fun login(@Body obj: JsonObject): LoginResponse

    @POST("users")
    suspend fun register(@Body obj: JsonObject): RegisterResponse

    @GET("genders")
    suspend fun getGenders() :ArrayList<ValueLabelResponse>

    @GET("asexualOrientations")
    suspend fun getSexualOrientations() : ArrayList<ValueLabelResponse>

    @GET("ageGroups")
    suspend fun getAgeGroups() : ArrayList<ValueLabelResponse>

    @GET("religions")
    suspend fun getReligions() : ArrayList<ValueLabelResponse>

    @GET("countries")
    suspend fun getCountries() : ArrayList<ValueLabelResponse>

    @GET("countries/{countryId}/states")
    suspend fun getStates(
        @Path("countryId") countryId : String
    ) : ArrayList<ValueLabelResponse>
}