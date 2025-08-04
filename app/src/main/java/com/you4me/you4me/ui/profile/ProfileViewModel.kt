package com.you4me.you4me.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.gson.JsonObject
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.models.useroptions.UserOptionsResponse
import com.you4me.you4me.models.verification.VeriffVerification
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val dbRepository: DbRepository,
) : ViewModel() {
    private val _getUserOptionsResponse: MutableLiveData<Resource<UserOptionsResponse>> =
        MutableLiveData()
    val getUserOptionsResponse: LiveData<Resource<UserOptionsResponse>>
        get() = _getUserOptionsResponse

    private val _genders: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val genders: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _genders

    private val _countries: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val countries: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _countries

    private val _sexualOrientations: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val sexualOrientations: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _sexualOrientations

    private val _ageGroups: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val ageGroups: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _ageGroups

    private val _religions: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val religions: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _religions

    private val _states: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        MutableLiveData()
    val states: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _states

    private val _getImagesAndVideos: MutableLiveData<Resource<ImagesVideosResponse>> =
        MutableLiveData()
    val getImagesAndVideos: LiveData<Resource<ImagesVideosResponse>>
        get() = _getImagesAndVideos

    val _dbUser: MutableLiveData<User> = MutableLiveData()
    val dbUser: LiveData<User>
        get() = _dbUser

    private val _user: MutableLiveData<Resource<User>> = MutableLiveData()
    val user: LiveData<Resource<User>>
        get() = _user

    private val _userDetails = SingleLiveEvent<Resource<User>>()
    val userDetails: LiveData<Resource<User>> get() = _userDetails

    private val _getSubscriptionStatus = SingleLiveEvent<Resource<GetSubscriptionStatus>>()
    val getSubscriptionStatus: LiveData<Resource<GetSubscriptionStatus>>
        get() = _getSubscriptionStatus

    private val _requestVeriffVerification: MutableLiveData<Resource<VeriffVerification>> =
        SingleLiveEvent()
    val requestVeriffVerification: LiveData<Resource<VeriffVerification>> get() = _requestVeriffVerification

    private val _updateUserResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val updateUserResponse: LiveData<Resource<Unit>>
        get() = _updateUserResponse

    private val _uploadVideoCloudinaryResponse: MutableLiveData<CloudinaryVideoUploadResponse> =
        SingleLiveEvent()
    val uploadVideoCloudinaryResponse: LiveData<CloudinaryVideoUploadResponse>
        get() = _uploadVideoCloudinaryResponse

    private val _uploadError: MutableLiveData<String> = SingleLiveEvent()
    val uploadError: LiveData<String> get() = _uploadError

    private val _sortPhotoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val sortPhotoUploadResponse: LiveData<Resource<Unit>>
        get() = _sortPhotoUploadResponse

    private val _registerPhotoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val registerPhotoUploadResponse: LiveData<Resource<Unit>>
        get() = _registerPhotoUploadResponse

    private val _registerVideoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val registerVideoUploadResponse: LiveData<Resource<Unit>>
        get() = _registerVideoUploadResponse

    private val _deleteVideoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val deleteVideoUploadResponse: LiveData<Resource<Unit>>
        get() = _deleteVideoUploadResponse

    private val _cameraUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val cameraUploadResponse: LiveData<Resource<Unit>>
        get() = _cameraUploadResponse

    private val _validateVideoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val validateVideoUpload: LiveData<Resource<Unit>>
        get() = _validateVideoUploadResponse

    private val _updateVideoUrlResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val updateVideoUrlResponse: LiveData<Resource<Unit>>
        get() = _updateVideoUrlResponse

    private val _deleteUserResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val deleteUserResponse: LiveData<Resource<Unit>>
        get() = _deleteUserResponse

    private val _logoutResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val logoutResponse: LiveData<Resource<Unit>>
        get() = _logoutResponse

    init {
        getUserFromDb()
        getUserOptions()
//        getGenders()
//        getSexualOrientations()
//        getAgeGroups()
//        getReligions()
    }

    fun logout(userId: String) {
        viewModelScope.launch {
            _logoutResponse.value = repository.logout(userId)
        }
    }

    fun deleteUser(userId: String, reason: JsonObject) {
        viewModelScope.launch {
            _deleteUserResponse.value = repository.deleteUser(userId, reason)
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            dbRepository.clear()
            dbRepository.insertUser(user)
        }
        _dbUser.value = user
    }

    fun getUserFromDb() {
        viewModelScope.launch {
            _dbUser.value = dbRepository.getUser()
        }
    }

    fun getUserDetails(userId: String) {
        viewModelScope.launch {
            _user.value = repository.getUser(userId)
        }
    }

    fun getUserProfileDetails(userId: String) {
        viewModelScope.launch {
            _userDetails.value = repository.getUser(userId)
        }
    }

    fun getSubscriptionStatus(userId: String) {
        viewModelScope.launch {
            _getSubscriptionStatus.value = repository.getSubscriptionStatus(userId)
        }
    }

    fun requestVerification(userId: String) {
        viewModelScope.launch {
            _requestVeriffVerification.value = repository.requestVerification(userId)
        }
    }

    fun getUserOptions() {
        viewModelScope.launch {
            _getUserOptionsResponse.value = repository.getUserOptions()
        }
    }

    fun getGenders() {
        viewModelScope.launch {
//            _genders.value = repository.getGenders()
        }
    }

//    fun getCountries() {
//        viewModelScope.launch {
//            _countries.value = repository.getCountries()
//        }
//    }

    fun getSexualOrientations() {
        viewModelScope.launch {
//            _sexualOrientations.value = repository.getSexualOrientations()
        }
    }

    fun getAgeGroups() {
        viewModelScope.launch {
//            _ageGroups.value = repository.getAgeGroups()
        }
    }

    fun getReligions() {
        viewModelScope.launch {
//            _religions.value = repository.getReligions()
        }
    }

    fun getStates(countryId: String) {
        viewModelScope.launch {
            _states.value = repository.getStates(countryId)
        }
    }

    fun getImagesAndVideos(userId: String) {
        viewModelScope.launch {
            _getImagesAndVideos.value = repository.getImageVideoUpload(userId)
        }
    }

    fun updateUser(userBody: UpdateUserBody) {
        val u = dbUser.value!!
        val uUser =
            User(
                u.userId,
                u.agePreferred,
                u.convertedDate,
                userBody.country,
                userBody.dob,
                u.email,
                userBody.gender,
                u.isVideoBeingReviewed,
                userBody.name,
                userBody.bio,
                u.completionPercentage,
                u.password,
                "",
                u.religionPreferred,
                userBody.religion_preferred,
                userBody.sexual_orientation,
                userBody.state,
                u.status,
                u.token,
                u.videoStatus,
                u.videoURL,
                u.isVerified ?: false
            )
        saveUser(uUser)
    }

//    fun updateUserInfo(user: User, userBody: UpdateUserBody) {
//        viewModelScope.launch {
//            val obj = JsonObject()
//            userBody.apply {
//                obj.addProperty("bio", bio)
//                obj.addProperty("name", name)
//                obj.addProperty("country", country)
//                obj.addProperty("state", state)
//                obj.addProperty("age_preferred", age_preferred)
//                obj.addProperty("religion_preferred", religion_preferred)
//                obj.addProperty("sexual_orientation", sexual_orientation)
//                obj.addProperty("dob", dob)
//                obj.addProperty("gender", gender)
//            }
//            Log.d("DB_CHECKING", _dbUser.value!!.userId)
//
//            _updateUserResponse.value = repository.updateUserInfo(user.userId, obj)
//        }
//    }

    fun updateUserInfo(userBody: UpdateUserBody, userId: String) {
        viewModelScope.launch {
            val obj = JsonObject()
            userBody.apply {
                obj.addProperty("bio", bio)
                obj.addProperty("name", name)
                obj.addProperty("country", country)
                obj.addProperty("state", state)
                obj.addProperty("age_preferred", age_preferred)
                obj.addProperty("religion_preferred", religion_preferred)
                obj.addProperty("sexual_orientation", sexual_orientation)
                obj.addProperty("dob", dob)
                obj.addProperty("gender", gender)
            }

            if (userId == null) {
                Log.e("ProfileViewModel", "User ID is null! Cannot update user info.")
                return@launch
            }

            Log.d("DB_CHECKING", userId)

            _updateUserResponse.value = repository.updateUserInfo(userId, obj)
        }
    }

//    fun uploadVideo(
//        videoUri: Uri,
//        videoId: String,
//    ) {
//        viewModelScope.launch {
//            MediaManager.get()
//                .upload(videoUri)
//                .option("resource_type", "auto")
//                .option("public_id", videoId)
//                .callback(
//                    object : UploadCallback {
//                        override fun onStart(requestId: String?) {
//                        }
//
//                        override fun onProgress(
//                            requestId: String?,
//                            bytes: Long,
//                            totalBytes: Long,
//                        ) {
//                        }
//
//                        override fun onSuccess(
//                            requestId: String?,
//                            resultData: MutableMap<Any?, Any?>?,
//                        ) {
//                            _uploadVideoCloudinaryResponse.value =
//                                resultData?.let {
//                                    CloudinaryVideoUploadResponse.from(
//                                        it,
//                                    )
//                                }
//                        }
//
//                        override fun onError(
//                            requestId: String?,
//                            error: ErrorInfo?,
//                        ) {
//                        }
//
//                        override fun onReschedule(
//                            requestId: String?,
//                            error: ErrorInfo?,
//                        ) {
//                        }
//                    },
//                ).dispatch()
//        }
//    }


    fun uploadVideo(videoUri: Uri, videoId: String, attempt: Int = 1) {
        viewModelScope.launch {
            MediaManager.get()
                .upload(videoUri)
                .option("resource_type", "auto")
                .option("public_id", videoId)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}

                    override fun onProgress(
                        requestId: String?,
                        bytes: Long,
                        totalBytes: Long
                    ) {
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        _uploadVideoCloudinaryResponse.value = resultData?.let {
                            CloudinaryVideoUploadResponse.from(it)
                        }
                    }

                    override fun onError(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                        val errorMessage = when (error?.code) {
                            502, 503 -> "Cloudinary is temporarily unavailable. Retrying..."
                            400 -> "Invalid request. Please check your video file."
                            401 -> "Unauthorized. Please check your API credentials."
                            else -> error?.description ?: "Unknown error occurred"
                        }

                        // Update LiveData to notify UI
                        _uploadError.postValue(errorMessage)

                        // Retry logic for 502 and 503 errors
                        if (error?.code == 502 || error?.code == 503) {
                            retryUpload(videoUri, videoId)
                        }
                    }

                    override fun onReschedule(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {
                    }
                })
                .dispatch()
        }
    }

    fun cameraUpload() {
        viewModelScope.launch {
            _cameraUploadResponse.value = repository.validateVideoUpload(_dbUser.value!!.userId)
        }
    }

    fun validateVideoUpload() {
        viewModelScope.launch {
            _validateVideoUploadResponse.value =
                repository.validateVideoUpload(_dbUser.value!!.userId)
        }
    }

    fun sortPhotoUpload(sortUploadsRequest: SortUploadsRequest) {
        viewModelScope.launch {
            _sortPhotoUploadResponse.value =
                repository.sortPhotoUpload(_dbUser.value!!.userId, sortUploadsRequest)
        }
    }

    fun registerProfilePhotoUpload(registerVideoUploadBody: RegisterProfilePhotoBody) {
        viewModelScope.launch {
            _registerPhotoUploadResponse.value =
                repository.registerProfilePhotoUpload(
                    _dbUser.value!!.userId,
                    registerVideoUploadBody
                )
        }
    }

    fun registerVideoUpload(registerVideoUploadBody: RegisterVideoUploadBody) {
        viewModelScope.launch {
            _registerVideoUploadResponse.value =
                repository.registerVideoUpload(_dbUser.value!!.userId, registerVideoUploadBody)
        }
    }

    fun deleteVideoUpload(videoId: String) {
        viewModelScope.launch {
            _deleteVideoUploadResponse.value =
                repository.deleteVideoUpload(_dbUser.value!!.userId, videoId)
        }
    }

    fun updateVideoUrl(
        videoId: String,
        videoUrl: String,
    ) {
        val obj = JsonObject()
        obj.addProperty("video_url", videoUrl)
        viewModelScope.launch {
            _updateVideoUrlResponse.value = repository.updateVideoUrl(videoId, obj)
        }
    }

    private fun retryUpload(videoUri: Uri, videoId: String, attempt: Int = 1) {
        val maxRetries = 5
        val delayMillis =
            (2.0.pow(attempt) * 1000L).toLong() // Exponential backoff (2^attempt * 1000ms)

        if (attempt > maxRetries) {
            Log.e("Upload", "Max retries reached. Upload failed.")
            return
        }

        viewModelScope.launch {
            delay(delayMillis) // Wait before retrying
            Log.d("Upload", "Retrying upload (Attempt $attempt)...")
            uploadVideo(videoUri, videoId, attempt + 1)
        }
    }
}
