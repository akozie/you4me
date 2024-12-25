package com.you4me.you4me.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.models.FetchDatesResponse
import com.you4me.you4me.models.GetSubscriptionStatus
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.Notification
import com.you4me.you4me.models.Place
import com.you4me.you4me.models.ProposeNewDateTimeBody
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.models.UpdateDateInterestBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    val repository: MainRepository,
    private val dbRepository: DbRepository,
) : ViewModel() {
    val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User>
        get() = _user

    private val _existingUser: MutableLiveData<Resource<User>> = MutableLiveData()
    val existingUser: LiveData<Resource<User>>
        get() = _existingUser

    private val _paymentModes = MutableLiveData<Resource<ArrayList<ValueLabelResponse>>>()
    val paymentModes: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _paymentModes

    private val _submitDateResponse = SingleLiveEvent<Resource<Unit>>()
    val submitDateResponse: LiveData<Resource<Unit>>
        get() = _submitDateResponse

    private val _fetchDates = SingleLiveEvent<Resource<FetchDatesResponse>>()
    val fetchDates: LiveData<Resource<FetchDatesResponse>>
        get() = _fetchDates

    private val _addSwipe = SingleLiveEvent<Resource<Unit>>()
    val addSwipe: LiveData<Resource<Unit>>
        get() = _addSwipe

    private val _addDateInterest = SingleLiveEvent<Resource<Unit>>()
    val addDateInterest: LiveData<Resource<Unit>>
        get() = _addDateInterest

    private val _fetchDateInterests = SingleLiveEvent<Resource<FetchDateInterest>>()
    val fetchDateInterests: LiveData<Resource<FetchDateInterest>>
        get() = _fetchDateInterests

    private val _getSubscriptionStatus = SingleLiveEvent<Resource<GetSubscriptionStatus>>()
    val getSubscriptionStatus: LiveData<Resource<GetSubscriptionStatus>>
        get() = _getSubscriptionStatus

    private val _getSubscriptionStatusForHome = SingleLiveEvent<Resource<GetSubscriptionStatus>>()
    val getSubscriptionStatusForHome: LiveData<Resource<GetSubscriptionStatus>>
        get() = _getSubscriptionStatusForHome

    private val _rejectDateInterest = SingleLiveEvent<Resource<Unit>>()
    val rejectDateInterest: LiveData<Resource<Unit>>
        get() = _rejectDateInterest

    private val _updateDateInterest = SingleLiveEvent<Resource<Unit>>()
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

    private val _getNotificationsResponse = MutableLiveData<Resource<ArrayList<Notification>>>()
    val getNotificationsResponse: LiveData<Resource<ArrayList<Notification>>>
        get() = _getNotificationsResponse

    private val _updateFirebaseTokenResponse = MutableLiveData<Resource<Unit>>()
    val updateFirebaseTokenResponse: LiveData<Resource<Unit>>
        get() = _updateFirebaseTokenResponse

    val _updatePaymentResponse = MutableLiveData<Resource<Unit>>()
    val updatePaymentResponse: LiveData<Resource<Unit>>
        get() = _updatePaymentResponse

    init {
        getUser()
        // fetchPaymentModes()
    }

    fun getUser() {
        viewModelScope.launch {
            _user.value = dbRepository.getUser()
        }
    }

    fun getNotifications() {
        viewModelScope.launch {
            _getNotificationsResponse.value = repository.getNotifications(_user.value?.userId ?: "")
//            Log.d("NOTIFICATION", user.value.toString())
        }
    }

    fun updatePushToken(token: JsonObject) {
        viewModelScope.launch {
            Log.d("FIREBASE", _user.value.toString())
            _updateFirebaseTokenResponse.value = repository.updatePushToken(_user.value?.userId ?: "", token)
        }
    }

    fun pushToken(
        token: JsonObject,
        userId: String,
    ) {
        viewModelScope.launch {
            Log.d("NEW_FIREBASE", _user.value.toString())
            _updateFirebaseTokenResponse.value = repository.updatePushToken(userId, token)
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun fetchPaymentModes() {
        Log.d("ME--me", "ME")
        viewModelScope.launch {
            _paymentModes.value = repository.getPaymentModes()
        }
    }

    fun submitDate(
        date: String,
        paymentMode: String,
        place: String,
        time: String,
    ) {
        val submitDateBody =
            SubmitDateBody(
                date,
                paymentMode,
                Place(place),
                time,
                user.value?.userId ?: "",
            )
        viewModelScope.launch {
            _submitDateResponse.value = repository.submitDate(submitDateBody)
        }
    }

    fun fetchDates() {
        viewModelScope.launch(Dispatchers.IO) {
            _fetchDates.postValue(repository.fetchDates(_user.value?.userId ?: ""))
        }
    }

    fun addDateInterest(
        dateId: String,
        date: String,
        time: String,
        proposer: String,
    ) {
        viewModelScope.launch {
            _addDateInterest.value =
                repository.addDateInterest(
                    dateId,
                    AddDateInterestBody(
                        date,
                        time,
                        _user.value?.userId ?: "",
                        proposer,
                    ),
                )
        }
    }

    fun addSwipe(
        dateId: String,
        personId: String,
        like: Boolean,
    ) {
        viewModelScope.launch {
            _addSwipe.value =
                repository.addSwipe(
                    AddSwipeBody(
                        if (like) "like" else "dislike",
                        dateId,
                        personId,
                        _user.value?.userId ?: "",
                    ),
                )
        }
    }

    fun fetchDateInterests() {
        viewModelScope.launch {
            _fetchDateInterests.value = repository.fetchDateInterest(_user.value?.userId ?: "")
        }
    }

    fun getSubscriptionStatus() {
        viewModelScope.launch {
            _getSubscriptionStatus.value = repository.getSubscriptionStatus(_user.value?.userId ?: "")
        }
    }

    fun getSubscriptionStatusForHome(userId: String) {
        viewModelScope.launch {
            _getSubscriptionStatusForHome.value = repository.getSubscriptionStatus(userId)
        }
    }

    fun rejectDateInterest(
        interestId: String,
        dateId: String,
        status: String,
    ) {
        viewModelScope.launch {
            _rejectDateInterest.value =
                repository.rejectDateInterest(
                    interestId,
                    RejectDateInterestBody(
                        dateId,
                        status,
                    ),
                )
        }
    }

    fun updateDateInterest(
        interestId: String,
        dateId: String,
        status: String,
    ) {
        viewModelScope.launch {
            _updateDateInterest.value =
                repository.acceptDateInterest(
                    interestId,
                    UpdateDateInterestBody(dateId, status, _user.value?.userId ?: ""),
                )
        }
    }

    fun getUpcomingDates() {
        viewModelScope.launch {
            _upcomingDates.value = repository.getUpcomingDates(_user.value?.userId ?: "")
        }
    }

    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            _existingUser.value = repository.getExistingUser(userId)
            // _user.value = existingUser.value
        }
    }

    fun getInviteeDatesRequiringApproval() {
        viewModelScope.launch {
            _inviteeDatesRequiringApproval.value =
                repository.inviteeDatesRequiringApproval(_user.value?.userId ?: "")
        }
    }

    fun getDateInterestsRequiringApproval() {
        viewModelScope.launch {
            _dateInterestsRequiringApproval.value =
                repository.getDateInterestsRequiringApproval(_user.value?.userId ?: "")
        }
    }

    fun proposeNewDateTime(
        dateId: String,
        interestId: String,
        proposedDate: String,
        proposedTime: String,
    ) {
        viewModelScope.launch {
            _proposeNewDateTime.value =
                repository.proposeNewDateTime(
                    user.value?.userId ?: "",
                    interestId,
                    ProposeNewDateTimeBody(dateId, interestId, proposedDate, proposedTime),
                )
        }
    }

    fun registerPayment(obj: JsonObject) {
        viewModelScope.launch { _updatePaymentResponse.value = repository.registerPayment(obj) }
    }
}
