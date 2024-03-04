package com.you4me.you4me.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you4me.you4me.models.AcceptDateInterestBody
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.models.FetchDatesResponse
import com.you4me.you4me.models.GetSubscriptionStatus
import com.you4me.you4me.models.Place
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
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

    private val _submitDateResponse = SingleLiveEvent<Resource<Unit>>()
    val submitDateResponse: LiveData<Resource<Unit>>
        get() = _submitDateResponse

    val _fetchDates = SingleLiveEvent<Resource<FetchDatesResponse>>()
    val fetchDates: LiveData<Resource<FetchDatesResponse>>
        get() = _fetchDates

    val _addSwipe = MutableLiveData<Resource<Unit>>()
    val addSwipe: LiveData<Resource<Unit>>
        get() = _addSwipe

    val _addDateInterest = MutableLiveData<Resource<Unit>>()
    val addDateInterest: LiveData<Resource<Unit>>
        get() = _addDateInterest

    val _fetchDateInterests = SingleLiveEvent<Resource<FetchDateInterest>>()
    val fetchDateInterests: LiveData<Resource<FetchDateInterest>>
        get() = _fetchDateInterests

    val _getSubscriptionStatus = SingleLiveEvent<Resource<GetSubscriptionStatus>>()
    val getSubscriptionStatus: LiveData<Resource<GetSubscriptionStatus>>
        get() = _getSubscriptionStatus

    val _rejectDateInterest = MutableLiveData<Resource<Unit>>()
    val rejectDateInterest: LiveData<Resource<Unit>>
        get() = _rejectDateInterest

    val _acceptDateInterest = MutableLiveData<Resource<Unit>>()
    val acceptDateInterest: LiveData<Resource<Unit>>
        get() = _acceptDateInterest

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

    fun fetchDates() {
        viewModelScope.launch {
            _fetchDates.value = repository.fetchDates(user.value!!.userId)
        }
    }

    fun addDateInterest(date: String, time: String, proposer: String) {
        viewModelScope.launch {
            _addDateInterest.value = repository.addDateInterest(
                AddDateInterestBody(
                    date,
                    time,
                    proposer,
                    user.value!!.userId
                )
            )
        }
    }

    fun addSwipe(dateId: String, personId: String, like: Boolean) {
        viewModelScope.launch {
            _addSwipe.value = repository.addSwipe(
                AddSwipeBody(
                    if (like) "like" else "dislike",
                    dateId,
                    personId,
                    user.value!!.userId
                )
            )
        }
    }

    fun fetchDateInterests() {
        viewModelScope.launch {
            _fetchDateInterests.value = repository.fetchDateInterest(user.value!!.userId)
        }
    }

    fun getSubscriptionStatus() {
        viewModelScope.launch {
            _getSubscriptionStatus.value = repository.getSubscriptionStatus(user.value!!.userId)
        }
    }

    fun rejectDateInterest(interestId: String, rejectDate: RejectDateInterestBody) {
        viewModelScope.launch {
            _rejectDateInterest.value = repository.rejectDateInterest(interestId, rejectDate)
        }
    }

    fun acceptDateInterest(interestId: String, dateId: String) {
        viewModelScope.launch {
            _acceptDateInterest.value = repository.acceptDateInterest(
                interestId,
                AcceptDateInterestBody(dateId, "PENDING_TIME_APPROVAL", user.value!!.userId)
            )
        }
    }
}