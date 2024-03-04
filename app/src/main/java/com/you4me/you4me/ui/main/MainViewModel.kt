package com.you4me.you4me.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you4me.you4me.models.Place
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.MainRepository
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: MainRepository,
    private val dbRepository: DbRepository
) : ViewModel() {

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User>
        get() = _user


    private val _paymentModes = MutableLiveData<Resource<ArrayList<ValueLabelResponse>>>()
    val paymentModes: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _paymentModes

    private val _submitDateResponse = MutableLiveData<Resource<Unit>>()
    val submitDateResponse: LiveData<Resource<Unit>>
        get() = _submitDateResponse

    init {
        getUser()
        fetchPaymentModes()
    }

    private fun getUser() {
        viewModelScope.launch {
            _user.value = dbRepository.getUser()
        }
    }

    private fun fetchPaymentModes() {
        viewModelScope.launch {
            _paymentModes.value = repository.getPaymentModes()
        }
    }

    fun submitDate(date: String, paymentMode: String, place: String, time: String) {
        val submitDateBody = SubmitDateBody(
            date,
            paymentMode,
            Place(place),
            time,
            _user.value!!.userId
        )
        viewModelScope.launch {
            _submitDateResponse.value = repository.submitDate(submitDateBody)
        }
    }
}