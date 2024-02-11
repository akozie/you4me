package com.you4me.you4me.ui.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you4me.you4me.models.LoginResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthenticationViewModel(private val repository: AuthenticationRepository) : ViewModel() {

    private val _loginResponse :MutableLiveData<Resource<LoginResponse>> = MutableLiveData()
    val loginResponse : LiveData<Resource<LoginResponse>>
        get() = _loginResponse

    fun login(email: String, password: String) {
        println("Loginnn")
        val obj = JSONObject()
        obj.put("email", email)
        obj.put("password", password)
        viewModelScope.launch {
            _loginResponse.value = repository.login(obj)
        }
    }
}