package com.you4me.you4me.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you4me.you4me.models.UpdateDateInterestBody
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.models.FetchDatesResponse
import com.you4me.you4me.models.GetSubscriptionStatus
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.Place
import com.you4me.you4me.models.ProposeNewDateTimeBody
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.UpcomingDates
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

    private val _fetchDates = SingleLiveEvent<Resource<FetchDatesResponse>>()
    val fetchDates: LiveData<Resource<FetchDatesResponse>>
        get() = _fetchDates

    private val _addSwipe = MutableLiveData<Resource<Unit>>()
    val addSwipe: LiveData<Resource<Unit>>
        get() = _addSwipe

    private val _addDateInterest = MutableLiveData<Resource<Unit>>()
    val addDateInterest: LiveData<Resource<Unit>>
        get() = _addDateInterest

    private val _fetchDateInterests = SingleLiveEvent<Resource<FetchDateInterest>>()
    val fetchDateInterests: LiveData<Resource<FetchDateInterest>>
        get() = _fetchDateInterests

    private val _getSubscriptionStatus = SingleLiveEvent<Resource<GetSubscriptionStatus>>()
    val getSubscriptionStatus: LiveData<Resource<GetSubscriptionStatus>>
        get() = _getSubscriptionStatus

    private val _rejectDateInterest = MutableLiveData<Resource<Unit>>()
    val rejectDateInterest: LiveData<Resource<Unit>>
        get() = _rejectDateInterest

    private val _updateDateInterest = MutableLiveData<Resource<Unit>>()
    val updateDateInterest: LiveData<Resource<Unit>>
        get() = _updateDateInterest

    private val _upcomingDates = SingleLiveEvent<Resource<UpcomingDates>>()
    val upcomingDates: LiveData<Resource<UpcomingDates>>
        get() = _upcomingDates

    private val _inviteeDatesRequiringApproval =
        SingleLiveEvent<Resource<InviteeDatesRequiringApproval>>()
    val inviteeDatesRequiringApproval: LiveData<Resource<InviteeDatesRequiringApproval>>
        get() = _inviteeDatesRequiringApproval

    private val _dateInterestsRequiringApproval =
        SingleLiveEvent<Resource<DateInterestsRequiringApproval>>()
    val dateInterestsRequiringApproval: LiveData<Resource<DateInterestsRequiringApproval>>
        get() = _dateInterestsRequiringApproval

    private val _proposeNewDateTime = SingleLiveEvent<Resource<Unit>>()
    val proposeNewDateTime: LiveData<Resource<Unit>>
        get() = _proposeNewDateTime

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

    fun rejectDateInterest(interestId: String, dateId: String, status: String) {
        viewModelScope.launch {
            _rejectDateInterest.value = repository.rejectDateInterest(
                interestId, RejectDateInterestBody(
                    dateId, status
                )
            )
        }
    }

    fun updateDateInterest(interestId: String, dateId: String, status: String) {
        viewModelScope.launch {
            _updateDateInterest.value = repository.acceptDateInterest(
                interestId,
                UpdateDateInterestBody(dateId, status, user.value!!.userId)
            )
        }
    }

    fun getUpcomingDates() {
        viewModelScope.launch {
            _upcomingDates.value = repository.getUpcomingDates(user.value!!.userId)
        }
    }

    fun getInviteeDatesRequiringApproval() {
        viewModelScope.launch {
            _inviteeDatesRequiringApproval.value =
                repository.inviteeDatesRequiringApproval(user.value!!.userId)
        }
    }

    fun getDateInterestsRequiringApproval() {
        viewModelScope.launch {
            _dateInterestsRequiringApproval.value =
                repository.getDateInterestsRequiringApproval(user.value!!.userId)
        }
    }

    fun proposeNewDateTime(
        dateId: String,
        interestId: String,
        proposedDate: String,
        proposedTime: String
    ) {
        viewModelScope.launch {
            _proposeNewDateTime.value = repository.proposeNewDateTime(
                user.value!!.userId,
                interestId,
                ProposeNewDateTimeBody(dateId, interestId, proposedDate, proposedTime)
            )
        }
    }
}