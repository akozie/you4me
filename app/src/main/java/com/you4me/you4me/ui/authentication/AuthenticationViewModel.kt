package com.you4me.you4me.ui.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val repository: AuthenticationRepository, private val dbRepository: DbRepository) : ViewModel() {

    private val _getTokenResponse: MutableLiveData<Resource<GetTokenResponse>> =
        SingleLiveEvent<Resource<GetTokenResponse>>()
    val getTokenResponse: LiveData<Resource<GetTokenResponse>>
        get() = _getTokenResponse

    private val _loginResponse: MutableLiveData<Resource<User>> =
        SingleLiveEvent<Resource<User>>()
    val loginResponse: LiveData<Resource<User>>
        get() = _loginResponse

    private val _signWithGoogleLoginResponse: MutableLiveData<Resource<User>> =
        SingleLiveEvent<Resource<User>>()
    val signWithGoogleLoginResponse: LiveData<Resource<User>>
        get() = _signWithGoogleLoginResponse

    private val _registerResponse: MutableLiveData<Resource<RegisterResponse>> = SingleLiveEvent()
    val registerResponse: LiveData<Resource<RegisterResponse>>
        get() = _registerResponse

    private val _registerRequestPasswordResetResponse: MutableLiveData<Resource<RequestPasswordResetResponse>> = SingleLiveEvent()
    val registerRequestPasswordResetResponse: LiveData<Resource<RequestPasswordResetResponse>>
        get() = _registerRequestPasswordResetResponse

    private val _verifyCodeResponse: MutableLiveData<Resource<VerifyCodeResponse>> = SingleLiveEvent()
    val verifyCodeResponse: LiveData<Resource<VerifyCodeResponse>>
        get() = _verifyCodeResponse

    private val _resetPasswordResponse: MutableLiveData<Resource<ResetPasswordResponse>> = SingleLiveEvent()
    val resetPasswordResponse: LiveData<Resource<ResetPasswordResponse>>
        get() = _resetPasswordResponse

    private val _user: MutableLiveData<Resource<User>> = MutableLiveData()
    val user: LiveData<Resource<User>>
        get() = _user

    fun getToken(
        apiKey: String,
    ) {
        val obj = JsonObject()
        obj.addProperty("api_key", apiKey)
        viewModelScope.launch {
            _getTokenResponse.value = repository.getToken(obj)
        }
    }

    fun login(
        email: String,
        password: String,
    ) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("password", password)
        viewModelScope.launch {
            _loginResponse.value = repository.login(obj)
        }
    }

    fun register(
        email: String,
        password: String,
        referralCode: String,
    ) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("password", password)
        obj.addProperty("referral_code", referralCode)
        viewModelScope.launch {
            _registerResponse.value = repository.register(obj)
        }
    }
    fun requestPasswordReset(
        email: String,
    ) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        viewModelScope.launch {
            _registerRequestPasswordResetResponse.value = repository.requestPasswordReset(obj)
        }
    }
    fun verifyCode(
        email: String,
        code: String
    ) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("code", code)
        viewModelScope.launch {
            _verifyCodeResponse.value = repository.verifyCode(obj)
        }
    }
    fun resetPassword(
        resetToken: String,
        newPassword: String
    ) {
        val obj = JsonObject()
        obj.addProperty("reset_token", resetToken)
        obj.addProperty("new_password", newPassword)
        viewModelScope.launch {
            _resetPasswordResponse.value = repository.resetPassword(obj)
        }
    }

    fun signInWithGoogle(token: String) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("token", token)
        viewModelScope.launch {
            _signWithGoogleLoginResponse.value = repository.googleSignIn(jsonObject)
        }
    }

    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            _user.value = repository.getUser(userId)
        }
    }

    fun saveUser(user: com.you4me.you4me.model.User) {
        viewModelScope.launch { dbRepository.insertUser(user) }
    }

    fun clearUser() {
        viewModelScope.launch { dbRepository.clear() }
    }
}
