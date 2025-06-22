package com.you4me.you4me.repository

import android.util.Log
import com.google.gson.JsonObject
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.utils.UtilityParam
import com.you4me.you4me.utils.UtilityParam.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//abstract class BaseRepository {
//
//    suspend fun <T> safeApiCall(apiCall : suspend() -> T) : Resource<T> {
//        return withContext(Dispatchers.IO){
//            try {
//                Resource.Success(apiCall.invoke())
//            } catch (throwable : Throwable) {
//               when (throwable) {
//                   is HttpException -> {
//                       val body = throwable.response()?.errorBody()?.string()
//                       val obj = if (!body.isNullOrBlank()) JSONObject(body) else null
//
//                       Resource.Failure(
//                           false,
//                           throwable.code(),
//                           obj?.getString("message"),
//                           body
//                       )
//                   }
//                   else -> {
//                       Resource.Failure(isNetworkError = true, null, "Please check your internet", null)
//                   }
//               }
//            }
//        }
//    }
//}

//abstract class BaseRepository {
//
//    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
//        return withContext(Dispatchers.IO) {
//            try {
//                Resource.Success(apiCall.invoke())
//            } catch (throwable: Throwable) {
//                when (throwable) {
//                    is HttpException -> {
//                        val body = throwable.response()?.errorBody()?.string()
//                        val errorMessage = extractErrorMessage(body)
//
//                        if (throwable.code() == 401 && errorMessage == "Invalid token") {
//                            //
//                        }
//
//                        Resource.Failure(false, throwable.code(), errorMessage, body)
//                    }
//                    else -> {
//                        Resource.Failure(isNetworkError = true, null, "Please check your internet", null)
//                    }
//                }
//            }
//        }
//    }
//
//
//    private fun extractErrorMessage(body: String?): String? {
//        return try {
//            if (!body.isNullOrBlank() && body.startsWith("{")) {
//                JSONObject(body).getString("message")
//            } else {
//                body // Return plain string if it's not JSON
//            }
//        } catch (e: JSONException) {
//            body // Fallback to raw response if parsing fails
//        }
//    }
//
//    // ✅ Convert to suspend function
//}


abstract class BaseRepository {

    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Resource<T> {
        return withContext(Dispatchers.IO) {
            try {
                Resource.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is HttpException -> {
                        val body = throwable.response()?.errorBody()?.string()
                        val errorMessage = extractErrorMessage(body)

                        // 🛑 Handle Unauthorized (401) and retry request after token refresh
                        if (errorMessage != null) {
                            if (throwable.code() == 401 && errorMessage.contains("Invalid token")) {
                                val newToken = refreshToken()
                                return@withContext if (!newToken.isNullOrEmpty()) {
                                    Resource.Success(apiCall.invoke()) // Retry API call
                                } else {
                                    Resource.Failure(
                                        false,
                                        401,
                                        "Session expired. Please login again.",
                                        null
                                    )
                                }
                            }
                        }
                        Log.d("JUST_CHECKING===", errorMessage.toString())

                        Resource.Failure(false, throwable.code(), errorMessage, body)
                    }
                    else -> {
                        Resource.Failure(
                            isNetworkError = true,
                            null,
                            "Please check your internet",
                            null
                        )
                    }
                }
            }
        }
    }

    // ✅ Extracts error messages from API response
    private fun extractErrorMessage(body: String?): String? {
        return try {
            if (!body.isNullOrBlank() && body.startsWith("{")) {
                JSONObject(body).getString("message")
            } else {
                body // Return plain string if it's not JSON
            }
        } catch (e: JSONException) {
            body // Fallback to raw response if parsing fails
        }
    }

    // ✅ Refresh token when expired
    private suspend fun refreshToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiCollector::class.java)
                val obj = JsonObject().apply {
                    addProperty("api_key", UtilityParam.API_KEY)
                }
                val response = api.getNewToken(obj).execute()

                if (response.isSuccessful) {
                    val newToken = response.body()!!.token
                    SharedPrefManager.getInstance()
                        .saveString(APP_TOKEN, newToken) // Save new token
                    newToken
                } else {
                    null // Token refresh failed
                }
            } catch (e: Exception) {
                null // Handle token refresh failure
            }
        }
    }
}
