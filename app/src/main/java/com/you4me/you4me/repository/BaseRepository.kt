package com.you4me.you4me.repository

import com.you4me.you4me.network.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

abstract class BaseRepository {

    suspend fun <T> safeApiCall(apiCall : suspend() -> T) : Resource<T> {
        return withContext(Dispatchers.IO){
            try {
                Resource.Success(apiCall.invoke())

            } catch (throwable : Throwable) {
               when (throwable) {
                   is HttpException -> {
                       val body = throwable.response()?.errorBody()?.string()
                       val obj = body?.let { JSONObject(it) }

                       Resource.Failure(
                           false,
                           throwable.code(),
                           obj?.getString("message"),
                           body
                       )
                   }
                   else -> {
                       Resource.Failure(isNetworkError = true, null, "An error occurred", null)
                   }
               }
            }
        }
    }
}