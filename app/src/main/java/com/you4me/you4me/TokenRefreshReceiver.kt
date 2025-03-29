package com.you4me.you4me

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.utils.UtilityParam.BASE_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("TOKEN_REFRESHHH", "Triggering token refresh")
        refreshTokenInBackground(context)
    }
    private fun refreshTokenInBackground(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(ApiCollector::class.java)

                val sharedPrefs = SharedPrefManager.getInstance()
                val currentToken = sharedPrefs.getString(APP_TOKEN)

                val response = api.refreshToken("Bearer $currentToken").execute()

                if (response.isSuccessful) {
                    val newToken = response.body()?.token
                    if (!newToken.isNullOrEmpty()) {
                        sharedPrefs.saveString(APP_TOKEN, newToken)
                        Log.d("TOKEN_REFRESHHH", "Token successfully refreshed: $newToken")
                    } else {
                        Log.e("TOKEN_REFRESHHH", "Failed to refresh token")
                    }
                } else {
                    Log.e("TOKEN_REFRESHHH", "Error refreshing token: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("TOKEN_REFRESHHH", "Exception while refreshing token: ${e.message}")
            }
        }
    }

}
