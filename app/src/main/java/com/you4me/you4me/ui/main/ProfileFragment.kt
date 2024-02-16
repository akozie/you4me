package com.you4me.you4me.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment

class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding, ProfileRepository>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addListeners()
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))

    private fun addListeners() {
        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.ageGroups.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, AGE_GROUP_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.religions.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, RELIGION_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.countries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, COUNTRY_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.states.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, STATE_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
    }

    private fun setupSpinner(values: ArrayList<ValueLabelResponse>, spinner: Int) {
        val names = values.map {
            it.label
        }.toMutableList()
        names.add(0, "")

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            names
        ).also { adapter ->
            when (spinner) {
                SEXUAL_ORIENTATION_SPINNER -> binding.sexualOrientation.adapter = adapter
                AGE_GROUP_SPINNER -> binding.agePreference.adapter = adapter
                RELIGION_SPINNER -> binding.religionPreference.adapter = adapter
                COUNTRY_SPINNER -> binding.country.adapter = adapter
                STATE_SPINNER -> binding.state.adapter = adapter
            }
        }
    }

    companion object {
        const val SEXUAL_ORIENTATION_SPINNER = 1
        const val AGE_GROUP_SPINNER = 2
        const val RELIGION_SPINNER = 3
        const val COUNTRY_SPINNER = 4
        const val STATE_SPINNER = 5
    }

}