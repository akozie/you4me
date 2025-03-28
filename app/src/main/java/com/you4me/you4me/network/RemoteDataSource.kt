package com.you4me.you4me.network

import android.content.Context
import com.you4me.you4me.BuildConfig
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.SharedPrefHelper.Companion.APP_TOKEN
import com.you4me.you4me.utils.SharedPrefManager
import com.you4me.you4me.utils.UtilityParam.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//class RemoteDataSource {
//
//
//
//    fun <Api> buildApi(api: Class<Api>): Api {
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(OkHttpClient.Builder().also { client ->
//                if (BuildConfig.DEBUG) {
//                    val logging = HttpLoggingInterceptor()
//                    logging.setLevel(HttpLoggingInterceptor.Level.BODY)
//                    client.addInterceptor(logging)
//                }
//            }.build())
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(api)
//    }
//}

class RemoteDataSource {

    fun <Api> buildApi(api: Class<Api>): Api {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val originalRequest = chain.request()
                        val requestBuilder = originalRequest.newBuilder()
                        val path = originalRequest.url.encodedPath

                        val sharedPrefs = SharedPrefManager.getInstance()
                        val token = sharedPrefs.getString(APP_TOKEN)

                        if (!token.isNullOrEmpty()) {
                            when {
                                path.contains("auth/token") -> {
                                    // Do nothing (no Authorization header for this request)
                                }
                                path.contains("auth/refresh") -> {
                                    // Add token but remove "Bearer"
                                    requestBuilder.addHeader("Authorization", token)
                                }
                                else -> {
                                    // Add Bearer token for all other requests
                                    requestBuilder.addHeader("Authorization", "Bearer $token")
                                }
                            }
                        }

                        chain.proceed(requestBuilder.build())
                    }
                    .authenticator(TokenAuthenticator()) // Token refresh logic
                    .also { client ->
                        if (BuildConfig.DEBUG) {
                            val logging = HttpLoggingInterceptor()
                            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                            client.addInterceptor(logging)
                        }
                    }
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api)
    }
}

