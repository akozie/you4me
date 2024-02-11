package com.you4me.you4me.ui.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.you4me.you4me.models.LoginResponse
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthenticationViewModel(private val repository: AuthenticationRepository) : ViewModel() {

    private val _loginResponse: MutableLiveData<Resource<LoginResponse>> =
        SingleLiveEvent<Resource<LoginResponse>>()
    val loginResponse: LiveData<Resource<LoginResponse>>
        get() = _loginResponse

    private val _registerResponse: MutableLiveData<Resource<RegisterResponse>> =SingleLiveEvent()
    val registerResponse: LiveData<Resource<RegisterResponse>>
        get() = _registerResponse

    fun login(email: String, password: String) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("password", password)
        viewModelScope.launch {
            _loginResponse.value = repository.login(obj)
        }
    }

    fun register(email: String, password: String) {
        val obj = JsonObject()
        obj.addProperty("email", email)
        obj.addProperty("password", password)
        viewModelScope.launch {
            _registerResponse.value = repository.register(obj)
        }
    }
}