package com.you4me.you4me.network

import com.google.gson.JsonObject
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.FetchDatesResponse
import com.you4me.you4me.models.User
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.ValueLabelResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiCollector {

    @POST("login")
    suspend fun login(@Body obj: JsonObject): User

    @POST("users")
    suspend fun register(@Body obj: JsonObject): RegisterResponse

    @GET("genders")
    suspend fun getGenders(): ArrayList<ValueLabelResponse>

    @GET("sexualOrientations")
    suspend fun getSexualOrientations(): ArrayList<ValueLabelResponse>

    @GET("ageGroups")
    suspend fun getAgeGroups(): ArrayList<ValueLabelResponse>

    @GET("religions")
    suspend fun getReligions(): ArrayList<ValueLabelResponse>

    @GET("countries")
    suspend fun getCountries(): ArrayList<ValueLabelResponse>

    @GET("countries/{countryId}/states")
    suspend fun getStates(
        @Path("countryId") countryId: String
    ): ArrayList<ValueLabelResponse>

    @PATCH("users/{userId}")
    suspend fun updateUserInfo(
        @Path("userId") userId: String,
        @Body obj: JsonObject
    )

    @GET("users/{userId}/validate-upload")
    suspend fun validateVideoUpload(
        @Path("userId") userId: String,
    )

    @POST("users/{userId}/uploads")
    suspend fun registerVideoUpload(
        @Path("userId") userId: String,
        @Body registerVideoUploadBody: RegisterVideoUploadBody
    )

    @PATCH("videos/{videoID}")
    suspend fun updateVideoUrl(
        @Path("videoID") videoId: String,
        @Body obj: JsonObject
    )

    @GET("payment-modes")
    suspend fun getPaymentModes(): ArrayList<ValueLabelResponse>

    @POST("dates")
    suspend fun submitDate(@Body body: SubmitDateBody)

    @GET("dates")
    suspend fun fetchDates(
        @Query("user_id") userId : String
    ) : FetchDatesResponse

    @POST("dates/interests/{dateId}")
    suspend fun addDateInterest(@Body body: AddDateInterestBody)

    @POST("swipes")
    suspend fun addSwipe(@Body body: AddSwipeBody)
}