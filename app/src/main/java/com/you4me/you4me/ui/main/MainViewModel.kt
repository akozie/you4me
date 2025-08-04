package com.you4me.you4me.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.models.interests.Interest
import com.you4me.you4me.models.interests.ReceivedInterestResponse
import com.you4me.you4me.models.useroptions.UserOptionsResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import com.you4me.you4me.ui.main.messaging.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class MainViewModel(
    val repository: MainRepository,
    private val dbRepository: DbRepository,
) : ViewModel() {

    private var currentPage = 1
    private val pageLimit = 20
    private var allInterests = mutableListOf<Interest>()
    private var allSentInterests = mutableListOf<Interest>()
    private var currentIndex = 0
    private var hasNextPage = false



    //All Interests
    private val _allDateInterests = MutableLiveData<List<Interest>>()
    val allDateInterests: LiveData<List<Interest>> = _allDateInterests

    private var interestAccumulator = mutableListOf<Interest>()
    private var allInterestsCurrentPage = 1
    private var allInterestsHasNextPage = false
    private var isLoading = false


    var hasNavigatedToProfile = false

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

    private val _sendPushNotification = SingleLiveEvent<Resource<Unit>>()
    val sendPushNotification: LiveData<Resource<Unit>>
        get() = _sendPushNotification

    private val _addDateInterest = SingleLiveEvent<Resource<Unit>>()
    val addDateInterest: LiveData<Resource<Unit>>
        get() = _addDateInterest

    private val _currentInterest = SingleLiveEvent<Interest>()
    val currentInterest: LiveData<Interest>
        get() = _currentInterest

    private val _receivedInterestDateInterests = SingleLiveEvent<Resource<ReceivedInterestResponse>>()
    val receivedInterestDateInterests: LiveData<Resource<ReceivedInterestResponse>>
        get() = _receivedInterestDateInterests

    private val _sentInterestDateInterests = SingleLiveEvent<Resource<ReceivedInterestResponse>>()
    val sentInterestDateInterests: LiveData<Resource<ReceivedInterestResponse>>
        get() = _sentInterestDateInterests

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

    private val _completedDates =
        SingleLiveEvent<Resource<CompletedDateResponse>>()
    val completedDates: LiveData<Resource<CompletedDateResponse>>
        get() = _completedDates

    private val _proposeNewDateTime = SingleLiveEvent<Resource<Unit>>()
    val proposeNewDateTime: LiveData<Resource<Unit>>
        get() = _proposeNewDateTime

    private val _getUserOptionsResponse: MutableLiveData<Resource<UserOptionsResponse>> =
        MutableLiveData()
    val getUserOptionsResponse: LiveData<Resource<UserOptionsResponse>>
        get() = _getUserOptionsResponse

    private val _getNotificationsResponse = MutableLiveData<Resource<ArrayList<Notification>>>()
    val getNotificationsResponse: LiveData<Resource<ArrayList<Notification>>>
        get() = _getNotificationsResponse

    private val _updateFirebaseTokenResponse = MutableLiveData<Resource<Response<Unit>>>()
    val updateFirebaseTokenResponse: LiveData<Resource<Response<Unit>>>
        get() = _updateFirebaseTokenResponse

    val _updatePaymentResponse = MutableLiveData<Resource<Unit>>()
    val updatePaymentResponse: LiveData<Resource<Unit>>
        get() = _updatePaymentResponse

    val _updateReviewResponse = MutableLiveData<Resource<Unit>>()
    val updateReviewResponse: LiveData<Resource<Unit>>
        get() = _updateReviewResponse


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
            _updateFirebaseTokenResponse.value =
                repository.updatePushToken(_user.value?.userId ?: "", token)
        }
    }

    fun pushToken(
        token: JsonObject,
        userId: String,
    ) {
        viewModelScope.launch {
            _updateFirebaseTokenResponse.value = repository.updatePushToken(userId, token)
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun getUserOptions() {
        viewModelScope.launch {
            _getUserOptionsResponse.value = repository.getUserOptions()
        }
    }

    fun submitDate(
        date: String,
        paymentMode: String,
        place: String,
        time: String,
        userId: String,
        type: String,
    ) {
        val submitDateBody =
            SubmitDateBody(
                date,
                paymentMode,
                Place(place),
                time,
                userId,
                type
            )
        viewModelScope.launch {
            _submitDateResponse.value = repository.submitDate(submitDateBody)
        }
    }

    fun fetchDates(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _fetchDates.postValue(repository.fetchDates(userId))
        }
    }

    fun addDateInterest(
        dateId: String,
        date: String,
        time: String,
        userId: String,
        proposer: String,
    ) {
        viewModelScope.launch {
            _addDateInterest.value =
                repository.addDateInterest(
                    dateId,
                    AddDateInterestBody(
                        date,
                        time,
                        userId,
                        proposer,
                    ),
                )
        }
    }

    fun addSwipe(
        dateId: String,
        personId: String,
        like: Boolean,
        userId: String
    ) {
        viewModelScope.launch {
            _addSwipe.value =
                repository.addSwipe(
                    AddSwipeBody(
                        if (like) "like" else "dislike",
                        dateId,
                        personId,
                        userId,
                    ),
                )
        }
    }

    fun sendPushNotification(obj: JsonObject) {
        viewModelScope.launch {
            _sendPushNotification.value =
                repository.sendPushNotification(
                    obj,
                )
        }
    }

//    fun receivedDateInterests(userId: String) {
//        viewModelScope.launch {
//            _receivedInterestDateInterests.value = repository.receivedDateInterests(userId)
//        }
//    }

    fun loadInterestsForList(userId: String) {
        if (isLoading || !allInterestsHasNextPage && allInterestsCurrentPage > 1) return

        isLoading = true
        viewModelScope.launch {
            when (val result = repository.receivedDateInterests(userId, allInterestsCurrentPage, pageLimit)) {
                is Resource.Success -> {
                    val filtered = result.value.interests.filter {
                        it.status == "PENDING" || it.status == "TIME_APPROVAL_REQUIRED"
                    }

                    interestAccumulator.addAll(filtered)
                    _allDateInterests.value = interestAccumulator
                    allInterestsHasNextPage = result.value.has_next_page
                    allInterestsCurrentPage++
                }

                is Resource.Failure -> {
                    // Handle failure (e.g., show Toast or log)
                }
            }
            isLoading = false
        }
    }

    fun receivedDateInterests(userId: String, page: Int = 1) {
        viewModelScope.launch {
            val response = repository.receivedDateInterests(userId, page, pageLimit)
            if (response is Resource.Success) {
                if (page == 1) allInterests.clear()
                allInterests.addAll(response.value.interests.filter {
                    it.status == "TIME_PENDING_APPROVAL"
                })
                hasNextPage = response.value.has_next_page
                currentPage = page
                currentIndex = 0
            }
            _receivedInterestDateInterests.value = response
        }
    }
    fun sentDateInterests(userId: String, page: Int = 1) {
        viewModelScope.launch {
            val response = repository.sentDateInterests(userId, page, pageLimit)
            if (response is Resource.Success) {
                if (page == 1) allSentInterests.clear()
                allSentInterests.addAll(response.value.interests.filter {
                    it.status == "PENDING" || it.status == "PENDING_TIME_APPROVAL"
                })
                hasNextPage = response.value.has_next_page
                currentPage = page
                currentIndex = 0
            }
            _sentInterestDateInterests.value = response
        }
    }

    private fun getCurrentInterest(): Interest? {
        return allInterests.getOrNull(currentIndex)
    }

    fun moveToNextInterest(userId: String) {
        if (currentIndex + 1 < allInterests.size) {
            currentIndex++
            _receivedInterestDateInterests.value = Resource.Success(
                ReceivedInterestResponse(
                    interests = listOfNotNull(getCurrentInterest()),
                    page = currentPage,
                    limit = pageLimit,
                    has_next_page = hasNextPage
                )
            )
        } else if (hasNextPage) {
            receivedDateInterests(userId, currentPage + 1)
        }
    }

    fun fetchDateOneInterest(userId: String) {
        viewModelScope.launch {
            _fetchDateInterests.value = repository.fetchDateInterest(userId)
        }
    }

    fun fetchDateInterests(userId: String) {
        viewModelScope.launch {
            _fetchDateInterests.value = repository.fetchDateInterest(userId)
        }
    }

    fun getSubscriptionStatus(userId: String) {
        viewModelScope.launch {
            _getSubscriptionStatus.value = repository.getSubscriptionStatus(userId)
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


    fun getUpcomingDates(userId: String) {
        viewModelScope.launch {
            _upcomingDates.value = repository.getUpcomingDates(userId)
        }
    }

    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            _existingUser.value = repository.getExistingUser(userId)
            // _user.value = existingUser.value
        }
    }

    fun getInviteeDatesRequiringApproval(userId: String) {
        viewModelScope.launch {
            _inviteeDatesRequiringApproval.value =
                repository.inviteeDatesRequiringApproval(userId)
        }
    }

    fun getDateInterestsRequiringApproval() {
        viewModelScope.launch {
            _dateInterestsRequiringApproval.value =
                repository.getDateInterestsRequiringApproval(_user.value?.userId ?: "")
        }
    }

    fun fetchCompletedDates() {
        viewModelScope.launch {
            _completedDates.value =
                repository.fetchCompletedDates(_user.value?.userId ?: "")
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

    fun updateReview(obj: JsonObject) {
        viewModelScope.launch { _updateReviewResponse.value = repository.updateReview(obj) }
    }

    private val _getImagesAndVideos: MutableLiveData<Resource<ImagesVideosResponse>> =
        MutableLiveData()
    val getImagesAndVideos: LiveData<Resource<ImagesVideosResponse>>
        get() = _getImagesAndVideos

    fun getImagesAndVideos(userId: String) {
        viewModelScope.launch {
            _getImagesAndVideos.value = repository.getImageVideoUpload(userId)
        }
    }

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _newMessage = MutableLiveData<Message>()
    val newMessage: LiveData<Message> get() = _newMessage

    fun sendMessage(
        senderId: String,
        receiverId: String,
        message: String,
        senderName: String,
        chatId: String,
        receiverName: String,
    ) {
        repository.sendMessage(
            senderId,
            receiverId,
            chatId,
            message,
            senderName,
            receiverName
        ) { success ->
            if (success) {
                // Handle UI updates if needed
                val obj =
                    JsonObject().apply {
                        addProperty("sender_name", senderName)
                        addProperty("recipient_user_id", receiverId)
                        addProperty("date_id", chatId)
                    }
                sendPushNotification(obj)
            }
        }
    }

    fun listenForNewMessages(
        senderId: String,
        receiverId: String,
        chatId: String,
    ) {
        repository.listenForNewMessages(senderId, receiverId, chatId) { newMessage ->
            _newMessage.postValue(newMessage)
        }
    }

    private val _isRecipientTyping = MutableLiveData<Boolean>()
    val isRecipientTyping: LiveData<Boolean> get() = _isRecipientTyping

    fun setTypingStatus(
        isTyping: Boolean,
        senderId: String,
        recipientId: String,
        chatId: String,
    ) {
        repository.setTypingStatus(isTyping, senderId, recipientId, chatId)
    }

    fun listenForTyping(
        senderId: String,
        recipientId: String,
        chatId: String,
    ) {
        repository.observeTypingStatus(senderId, recipientId, chatId) { isTyping ->
            _isRecipientTyping.postValue(isTyping)
        }
    }

    fun markMessagesAsSeen(
        chatId: String,
        senderId: String,
        receiverId: String,
    ) {
        repository.markMessagesAsSeen(chatId, senderId, receiverId) {
            // After messages are marked as seen, reload messages to update UI
            loadMessages(chatId)
//            Log.d("OK_SEEN_VIEWMODEL", "OK_SEEN_VIEWMODEL")
        }
    }

    fun loadMessages(chatId: String) {
        repository.getMessages(chatId) { messagesList ->
            _messages.postValue(messagesList)
        }
    }
}
