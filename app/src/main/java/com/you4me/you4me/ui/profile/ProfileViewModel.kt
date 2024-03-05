package com.you4me.you4me.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.gson.JsonObject
import com.you4me.you4me.models.CloudinaryVideoUploadResponse
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.models.UpdateUserBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.DbRepository
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val repository: ProfileRepository,
    private val dbRepository: DbRepository
) : ViewModel() {

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

    private val _user: MutableLiveData<User> = MutableLiveData()
    val user: LiveData<User>
        get() = _user

    private val _updateUserResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val updateUserResponse: LiveData<Resource<Unit>>
        get() = _updateUserResponse

    private val _uploadVideoCloudinaryResponse: MutableLiveData<CloudinaryVideoUploadResponse> =
        SingleLiveEvent()
    val uploadVideoCloudinaryResponse: LiveData<CloudinaryVideoUploadResponse>
        get() = _uploadVideoCloudinaryResponse

    private val _registerVideoUploadResponse: MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val registerVideoUploadResponse: LiveData<Resource<Unit>>
        get() = _registerVideoUploadResponse

    private val _validateVideoUploadResponse : MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val validateVideoUpload : LiveData<Resource<Unit>>
        get() = _validateVideoUploadResponse

    private val _updateVideoUrlResponse : MutableLiveData<Resource<Unit>> = SingleLiveEvent()
    val updateVideoUrlResponse  : LiveData<Resource<Unit>>
        get() = _updateVideoUrlResponse

    init {
        getUser()
        getGenders()
        getCountries()
        getSexualOrientations()
        getAgeGroups()
        getReligions()
    }

    private fun getUser() {
        viewModelScope.launch {
            _user.value = dbRepository.getUser()
        }
    }

    private fun getGenders() {
        viewModelScope.launch {
            _genders.value = repository.getGenders()
        }
    }

    private fun getCountries() {
        viewModelScope.launch {
            _countries.value = repository.getCountries()
        }
    }

    private fun getSexualOrientations() {
        viewModelScope.launch {
            _sexualOrientations.value = repository.getSexualOrientations()
        }
    }

    private fun getAgeGroups() {
        viewModelScope.launch {
            _ageGroups.value = repository.getAgeGroups()
        }
    }

    private fun getReligions() {
        viewModelScope.launch {
            _religions.value = repository.getReligions()
        }
    }

    fun getStates(countryId: String) {
        viewModelScope.launch {
            _states.value = repository.getStates(countryId)
        }
    }

    fun updateUserInfo(userBody: UpdateUserBody) {
        viewModelScope.launch {
            val obj = JsonObject()
            userBody.apply {
                obj.addProperty("name", name)
                obj.addProperty("country", country)
                obj.addProperty("state", state)
                obj.addProperty("age_preferred", age_preferred)
                obj.addProperty("religion", religion)
                obj.addProperty("religion_preferred", religion_preferred)
                obj.addProperty("sexual_orientation", sexual_orientation)
                obj.addProperty("dob", dob)
                obj.addProperty("gender", gender)
                obj.addProperty("phone", phone)
            }

            _updateUserResponse.value = repository.updateUserInfo(_user.value!!.userId, obj)
        }
    }

    fun uploadVideo(videoUri: Uri, videoId : String) {
        viewModelScope.launch {
            MediaManager.get()
                .upload(videoUri)
                .option("resource_type", "auto")
                .option("public_id", videoId)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {

                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        _uploadVideoCloudinaryResponse.value = resultData?.let {
                            CloudinaryVideoUploadResponse.from(
                                it
                            )
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {

                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {

                    }

                }).dispatch()
        }
    }

    fun validateVideoUpload() {
        viewModelScope.launch {
            _validateVideoUploadResponse.value = repository.validateVideoUpload(_user.value!!.userId)
        }
    }

    fun registerVideoUpload(registerVideoUploadBody: RegisterVideoUploadBody) {
        viewModelScope.launch {
            _registerVideoUploadResponse.value =
                repository.registerVideoUpload(_user.value!!.userId, registerVideoUploadBody)
        }
    }

    fun updateVideoUrl(videoId: String, videoUrl: String) {
        val obj = JsonObject()
        obj.addProperty("video_url", videoUrl)
        viewModelScope.launch {
            _updateVideoUrlResponse.value = repository.updateVideoUrl(videoId, obj)
        }
    }
}