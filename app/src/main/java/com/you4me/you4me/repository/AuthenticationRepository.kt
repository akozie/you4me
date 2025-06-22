package com.you4me.you4me.repository

import com.google.gson.JsonObject
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource

class AuthenticationRepository(private val api: ApiCollector) : BaseRepository() {

    suspend fun getToken(obj: JsonObject): Resource<GetTokenResponse> {
        return safeApiCall {
            api.getToken(obj)
        }
    }

    suspend fun login(obj: JsonObject): Resource<User> {
        return safeApiCall {
            api.login(obj)
        }
    }


    suspend fun register(obj: JsonObject): Resource<RegisterResponse> {
        return safeApiCall {
            api.register(obj)
        }
    }

    suspend fun requestPasswordReset(obj: JsonObject): Resource<RequestPasswordResetResponse> {
        return safeApiCall {
            api.requestPasswordReset(obj)
        }
    }
    suspend fun verifyCode(obj: JsonObject): Resource<VerifyCodeResponse> {
        return safeApiCall {
            api.verifyCode(obj)
        }
    }

    suspend fun resetPassword(obj: JsonObject): Resource<ResetPasswordResponse> {
        return safeApiCall {
            api.resetPassword(obj)
        }
    }

    suspend fun googleSignIn(token: JsonObject) = safeApiCall { api.googleSignIn(token) }

    suspend fun getUser(userId: String) = safeApiCall { api.getUser(userId) }

}