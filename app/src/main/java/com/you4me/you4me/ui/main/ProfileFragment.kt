package com.you4me.you4me.ui.main

import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.models.User
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
        addObservers()
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))

    private fun addObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            populateViews(it)
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

    private fun addListeners() {
        binding.editBtn.setOnClickListener {
            when (binding.editBtn.text) {
                getText(R.string.update) -> {

                }

                getText(R.string.edit) -> {
                    switchProfile(true)
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
                SEXUAL_ORIENTATION_SPINNER -> binding.sexualOrientationSpinner.adapter = adapter
                AGE_GROUP_SPINNER -> binding.agePreferenceSpinner.adapter = adapter
                RELIGION_SPINNER -> binding.religionPreferenceSpinner.adapter = adapter
                COUNTRY_SPINNER -> binding.countrySpinner.adapter = adapter
                STATE_SPINNER -> binding.stateSpinner.adapter = adapter
            }
        }
    }

    private fun switchProfile(edit: Boolean) {
        binding.sexualOrientationSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.agePreferenceSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.religionPreferenceSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.countrySpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.stateSpinner.visibility = if (edit) View.VISIBLE else View.GONE

        binding.name.isEnabled = edit
        binding.editBtn.text = getText(if (edit) R.string.update else R.string.edit)

        binding.sexualOrientationTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.agePreferenceTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.religionPreferenceTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.countryTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.stateTxt.visibility = if (!edit) View.VISIBLE else View.GONE
    }

    private fun populateViews(user: User) {
        binding.name.setText(user.name)
        binding.countryTxt.text = user.country
        binding.stateTxt.text = user.state
        binding.religionPreferenceTxt.text = user.religion
        binding.agePreferenceTxt.text = user.agePreferred
        binding.sexualOrientationTxt.text = user.sexualOrientation
    }

    companion object {
        const val SEXUAL_ORIENTATION_SPINNER = 1
        const val AGE_GROUP_SPINNER = 2
        const val RELIGION_SPINNER = 3
        const val COUNTRY_SPINNER = 4
        const val STATE_SPINNER = 5
    }

}