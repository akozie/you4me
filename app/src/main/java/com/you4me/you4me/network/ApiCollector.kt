package com.you4me.you4me.network

import com.google.gson.JsonObject
import com.you4me.you4me.models.UpdateDateInterestBody
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.models.FetchDatesResponse
import com.you4me.you4me.models.GetSubscriptionStatus
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.ProposeNewDateTimeBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.models.ValueLabelResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiCollector {

    @POST("login")
    suspend fun login(@Body obj: JsonObject): User

    @POST("users")
    suspend fun register(@Body obj: JsonObject): RegisterResponse

    @POST("google-sign-on")
    suspend fun googleSignIn(@Body token: JsonObject): User

    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String
    ): User

    @PATCH("push-tokens/{userId}")
    suspend fun updatePushToken(
        @Path("userId") userId: String,
        @Body token: JsonObject
    )

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
        @Query("user_id") userId: String
    ): FetchDatesResponse

    @POST("dates/interests/{dateId}")
    suspend fun addDateInterest(@Body body: AddDateInterestBody)

    @POST("swipes")
    suspend fun addSwipe(@Body body: AddSwipeBody)

    @GET("users/{userId}/dates/interests")
    suspend fun fetchDateInterests(
        @Path("userId") userId: String
    ): FetchDateInterest

    @GET("users/{userId}/subscription/status")
    suspend fun getSubscriptionStatus(
        @Path("userId") userId: String
    ): GetSubscriptionStatus

    @PUT("dates/{interestId}/interests")
    suspend fun rejectDateInterest(
        @Path("interestId") interestId: String,
        @Body body: RejectDateInterestBody
    )

    @PUT("dates/{interestId}/interests")
    suspend fun updateDateInterest(
        @Path("interestId") interestId: String,
        @Body body: UpdateDateInterestBody
    )

    @GET("users/{userId}/dates/upcoming")
    suspend fun getUpcomingDates(
        @Path("userId") userId: String
    ): UpcomingDates

    @GET("users/{userId}/dates/interests/pending-approval")
    suspend fun getInviteeDatesRequiringApproval(
        @Path("userId") userId: String
    ): InviteeDatesRequiringApproval

    @GET("users/{userId}/dates/interests/creator/require-approval")
    suspend fun getDateInterestsRequiringApproval(
        @Path("userId") userId: String
    ): DateInterestsRequiringApproval

    @PUT("users/{userId}/dates/interests/{interestId}/propose-time")
    suspend fun proposeNewDateTime(
        @Path("userId") userId: String,
        @Path("interestId") interestId: String,
        @Body body: ProposeNewDateTimeBody
    )
}