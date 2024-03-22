package com.you4me.you4me.ui.authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.google.gson.JsonObject
import com.you4me.you4me.models.User
import com.you4me.you4me.models.RegisterResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.AuthenticationRepository
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.launch

class AuthenticationViewModel(private val repository: AuthenticationRepository, private val dbRepository: DbRepository) : ViewModel() {

    private val _loginResponse: MutableLiveData<Resource<User>> =
        SingleLiveEvent<Resource<User>>()
    val loginResponse: LiveData<Resource<User>>
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

    fun signInWithGoogle(token : String, isLogin : Boolean = true) {
        viewModelScope.launch {
            if (isLogin) {
                _loginResponse.value = repository.googleSignIn(token)
            }
        }
    }

    fun saveUser(user : User) {
        viewModelScope.launch { dbRepository.insertUser(user) }
    }

    fun clearUser() {
        viewModelScope.launch { dbRepository.clear() }
    }
}