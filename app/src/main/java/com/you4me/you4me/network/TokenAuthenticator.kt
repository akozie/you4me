package com.you4me.you4me.network

import android.util.Log
import com.google.gson.JsonObject
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.utils.UtilityParam.API_KEY
import com.you4me.you4me.utils.UtilityParam.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) { // Token expired
            synchronized(this) { // Ensure only one refresh happens at a time
                val sharedPrefs = SharedPrefManager.getInstance()
                val oldToken = sharedPrefs.getString(APP_TOKEN)
                Log.d("SEE_DIFF", "${response.request.header("Authorization")} == $oldToken")
                // If the request already contains the old token, do not retry infinitely
                if (response.request.header("Authorization") == "Bearer $oldToken") {
                    val newToken = getNewToken() // Call API to refresh token

                    return if (!newToken.isNullOrEmpty()) {
                        sharedPrefs.saveString(APP_TOKEN, newToken) // Save new token
                        response.request.newBuilder()
                            .header("Authorization", newToken)
                            .build() // Retry request with new token
                    } else {
                        null // Return null if refresh failed (force logout)
                    }
                }
            }
        }
        return null // Other errors are not handled here
    }

    private fun getNewToken(): String? {
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(ApiCollector::class.java)
            val obj = JsonObject().apply {
                addProperty("api_key", API_KEY)
            }

            // Call API synchronously
            val response = api.getNewToken(obj).execute()

            if (response.isSuccessful) {
                response.body()?.token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

