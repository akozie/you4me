package com.you4me.you4me.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.utils.UtilityParam.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val newToken = refreshToken()
            if (!newToken.isNullOrEmpty()) {
                SharedPrefManager.getInstance().saveString(APP_TOKEN, newToken)
                Log.d("TOKEN_REFRESH", "Token successfully refreshed: $newToken")
                Result.success()
            } else {
                Log.e("TOKEN_REFRESH", "Failed to refresh token")
                Result.retry() // Retry in case of failure
            }
        } catch (e: Exception) {
            Log.e("TOKEN_REFRESH", "Exception: ${e.message}")
            Result.retry()
        }
    }

    private suspend fun refreshToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiCollector::class.java)

                val sharedPrefs = SharedPrefManager.getInstance()
                val currentToken = sharedPrefs.getString(APP_TOKEN) ?: ""

                val response = api.refreshToken("$currentToken").execute()

                if (response.isSuccessful) {
                    val newToken = response.body()?.token
                    if (newToken != null) {
                        sharedPrefs.saveString(APP_TOKEN, newToken) // Save new token
                    }
                    newToken
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

}
