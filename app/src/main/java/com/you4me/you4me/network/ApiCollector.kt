package com.you4me.you4me.network

import com.google.gson.JsonObject
import com.you4me.you4me.models.*
import retrofit2.Call
import retrofit2.http.*

interface ApiCollector {

    @POST("auth/refresh")
     fun refreshToken(
        @Header("Authorization") token: String
    ): Call<GetTokenResponse>

    @POST("auth/token")
     fun getNewToken(
        @Body obj: JsonObject,
        ): Call<GetTokenResponse>

    @POST("login")
    suspend fun login(
        @Body obj: JsonObject,
    ): User

    @POST("auth/token")
    suspend fun getToken(
        @Body obj: JsonObject,
    ): GetTokenResponse

    @POST("logout/{userId}")
    suspend fun logout(
        @Path("userId") userId: String,
    )

    @POST("users")
    suspend fun register(
        @Body obj: JsonObject,
    ): RegisterResponse

    @POST("google-sign-on")
    suspend fun googleSignIn(
        @Body token: JsonObject,
    ): User

    @GET("users/{userId}")
    suspend fun getUser(
        @Path("userId") userId: String,
    ): User

    @GET("users/{userId}")
    suspend fun getExistingUser(
        @Path("userId") userId: String,
    ): User

    @PATCH("push-tokens/{userId}")
    suspend fun updatePushToken(
        @Path("userId") userId: String,
        @Body token: JsonObject,
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
        @Path("countryId") countryId: String,
    ): ArrayList<ValueLabelResponse>

    @POST("delete/{userId}")
    suspend fun deleteUser(
        @Path("userId") userId: String,
    )

    @PATCH("users/{userId}")
    suspend fun updateUserInfo(
        @Path("userId") userId: String,
        @Body obj: JsonObject,
    )

    @GET("users/{userId}/validate-upload")
    suspend fun validateVideoUpload(
        @Path("userId") userId: String,
    )

    @POST("users/{userId}/uploads")
    suspend fun registerVideoUpload(
        @Path("userId") userId: String,
        @Body registerVideoUploadBody: RegisterVideoUploadBody,
    )

    @DELETE("users/{userId}/uploads/{videoId}")
    suspend fun deleteVideoUpload(
        @Path("userId") userId: String,
        @Path("videoId") videoId: String,
    )

    @GET("users/{userId}/uploads")
    suspend fun getImageVideoUpload(
        @Path("userId") userId: String,
    ): ImagesVideosResponse

    @PATCH("videos/{videoID}")
    suspend fun updateVideoUrl(
        @Path("videoID") videoId: String,
        @Body obj: JsonObject,
    )

    @GET("payment-modes")
    suspend fun getPaymentModes(): ArrayList<ValueLabelResponse>

    @POST("dates")
    suspend fun submitDate(
        @Body body: SubmitDateBody,
    )

    @GET("dates")
    suspend fun fetchDates(
        @Query("user_id") userId: String,
    ): FetchDatesResponse

    @POST("dates/interests/{dateId}")
    suspend fun addDateInterest(
        @Path("dateId") dateId: String,
        @Body body: AddDateInterestBody,
    )

    @POST("swipes")
    suspend fun addSwipe(
        @Body body: AddSwipeBody,
    )

    @POST("notify-user")
    suspend fun sendPushNotification(
        @Body obj: JsonObject,
    )

    @GET("users/{userId}/dates/interests")
    suspend fun fetchDateInterests(
        @Path("userId") userId: String,
    ): FetchDateInterest

    @GET("users/{userId}/subscription/status")
    suspend fun getSubscriptionStatus(
        @Path("userId") userId: String,
    ): GetSubscriptionStatus

    @PUT("dates/{interestId}/interests")
    suspend fun rejectDateInterest(
        @Path("interestId") interestId: String,
        @Body body: RejectDateInterestBody,
    )

    @PUT("dates/{interestId}/interests")
    suspend fun updateDateInterest(
        @Path("interestId") interestId: String,
        @Body body: UpdateDateInterestBody,
    )

    @GET("users/{userId}/dates/upcoming")
    suspend fun getUpcomingDates(
        @Path("userId") userId: String,
    ): UpcomingDates

    @GET("users/{userId}/dates/interests/pending-approval")
    suspend fun getInviteeDatesRequiringApproval(
        @Path("userId") userId: String,
    ): InviteeDatesRequiringApproval

    @GET("users/{userId}/dates/interests/creator/require-approval")
    suspend fun getDateInterestsRequiringApproval(
        @Path("userId") userId: String,
    ): DateInterestsRequiringApproval

    @GET("users/{userId}/dates/confirmed")
    suspend fun fetchCompletedDates(
        @Path("userId") userId: String,
    ): CompletedDateResponse

    @PUT("users/{userId}/dates/interests/{interestId}/propose-time")
    suspend fun proposeNewDateTime(
        @Path("userId") userId: String,
        @Path("interestId") interestId: String,
        @Body body: ProposeNewDateTimeBody,
    )

    @GET("notifications/users/{userId}")
    suspend fun getNotifications(
        @Path("userId") userId: String,
    ): ArrayList<Notification>

    @PATCH("notifications/{notifyId}")
    suspend fun markNotificationAsRead(
        @Path("notifyId") notificationId: String,
    )

    @POST("payments")
    suspend fun registerPayment(
        @Body obj: JsonObject,
    )

    @POST("reviews")
    suspend fun updateReview(
        @Body obj: JsonObject,
    )

    @GET("users")
    suspend fun registerPayment(
        @Path("userId") userId: String,
    )

//    https://j3rmo1mo2a.execute-api.us-east-1.amazonaws.com/prod/dates/interests/3f0bdbc2-4ad8-42f9-91bb-1461675fa3f8
//    I/okhttp.OkHttpClient: Content-Type: application/json; charset=UTF-8
//    I/okhttp.OkHttpClient: Content-Length: 155
//    I/okhttp.OkHttpClient: {"proposedDate":"May 30, 2024","proposedTime":"12:00","submittedBy":"65b48397-bc37-43fa-85cc-4515a0cd4ff3","userId":"4691acb0-bb6a-475a-bafd-4e96eb4a705f"}
}
