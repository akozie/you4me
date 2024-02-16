package com.you4me.you4me.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.SingleLiveEvent
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _genders: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val genders: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _genders

    private val _countries: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val countries: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _countries

    private val _sexualOrientations: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val sexualOrientations: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _sexualOrientations

    private val _ageGroups: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val ageGroups: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _ageGroups

    private val _religions: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val religions: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _religions

    private val _states: MutableLiveData<Resource<ArrayList<ValueLabelResponse>>> =
        SingleLiveEvent()
    val states: LiveData<Resource<ArrayList<ValueLabelResponse>>>
        get() = _states


    init {
        getGenders()
        getCountries()
        getSexualOrientations()
        getAgeGroups()
        getReligions()
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

}