package com.you4me.you4me.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.models.UpdateUserBody
import com.you4me.you4me.models.User
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding, ProfileRepository>() {

    private lateinit var name: String
    private lateinit var agePreferred: String
    private lateinit var dob: String
    private lateinit var gender: String
    private lateinit var religionPreferred: String
    private lateinit var sexualOrientation: String
    private lateinit var state: String
    private lateinit var country: String
    private lateinit var user: User

    private lateinit var countries: ArrayList<ValueLabelResponse>
    private lateinit var states: ArrayList<ValueLabelResponse>
    private lateinit var agePreferences: ArrayList<ValueLabelResponse>
    private lateinit var genders: ArrayList<ValueLabelResponse>
    private lateinit var religiousPreferences: ArrayList<ValueLabelResponse>
    private lateinit var sexualOrientations: ArrayList<ValueLabelResponse>

    private lateinit var calendar: Calendar
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addListeners()
        addObservers()
        setupView()
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))

    private fun addObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            user = it
            populateViews(it)
        }
        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
                    sexualOrientations = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.ageGroups.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, AGE_GROUP_SPINNER)
                    agePreferences = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.religions.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, RELIGION_SPINNER)
                    religiousPreferences = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.countries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, COUNTRY_SPINNER)
                    countries = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
                    sexualOrientations = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.states.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isNotEmpty()) setupSpinner(it.value, STATE_SPINNER)
                    states = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.genders.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupSpinner(it.value, GENDER_SPINNER)
                    genders = it.value
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.updateUserResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    Toast.makeText(
                        requireContext(), "Profile Update Successful", Toast.LENGTH_SHORT
                    ).show()
                    switchProfile(false)
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "An error occurred")
                }
            }
        }
        viewModel.uploadVideoCloudinaryResponse.observe(viewLifecycleOwner) {
            viewModel.registerVideoUpload(
                RegisterVideoUploadBody(
                    it.url,
                    user.userId,
                    it.public_id
                )
            )
        }
        viewModel.registerVideoUploadResponse.observe(viewLifecycleOwner) {
            when(it){
                is Resource.Success -> {
                    showLoader(false)
                    Toast.makeText(requireContext(), "Video Upload Successful!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Failure -> {}
            }
        }
    }

    private fun addListeners() {
        binding.editBtn.setOnClickListener {
            when (binding.editBtn.text) {
                getText(R.string.update) -> {
                    showLoader(true)
                    viewModel.updateUserInfo(
                        UpdateUserBody(
                            agePreferred,
                            country,
                            dob,
                            gender,
                            binding.name.text.toString(),
                            religionPreferred,
                            sexualOrientation,
                            state
                        )
                    )
                }

                getText(R.string.edit) -> {
                    switchProfile(true)
                }
            }
        }

        binding.addVideoLyt.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }

        binding.stateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) return
                state = states[p2 - 1].value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

        binding.countrySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 == 0) return
                    if (countries[p2 - 1].value != country) viewModel.getStates(countries[p2 - 1].value)
                    country = countries[p2 - 1].value
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        binding.sexualOrientationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 == 0) return
                    sexualOrientation = sexualOrientations[p2 - 1].value
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        binding.religionPreferenceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 == 0) return
                    religionPreferred = religiousPreferences[p2 - 1].value
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        binding.agePreferenceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p2 == 0) return
                    agePreferred = agePreferences[p2 - 1].value
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) return
                gender = genders[p2 - 1].value
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun setupSpinner(values: ArrayList<ValueLabelResponse>, spinner: Int) {
        val names = values.map {
            it.label
        }.toMutableList()
        names.add(0, "Select")

        ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, names
        ).also { adapter ->
            when (spinner) {
                SEXUAL_ORIENTATION_SPINNER -> binding.sexualOrientationSpinner.adapter = adapter
                AGE_GROUP_SPINNER -> binding.agePreferenceSpinner.adapter = adapter
                RELIGION_SPINNER -> binding.religionPreferenceSpinner.adapter = adapter
                COUNTRY_SPINNER -> binding.countrySpinner.adapter = adapter
                STATE_SPINNER -> binding.stateSpinner.adapter = adapter
                GENDER_SPINNER -> binding.genderSpinner.adapter = adapter
            }
        }
    }

    private fun switchProfile(edit: Boolean) {
        binding.sexualOrientationSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.agePreferenceSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.religionPreferenceSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.countrySpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.stateSpinner.visibility = if (edit) View.VISIBLE else View.GONE
        binding.dob.visibility = if (edit) View.VISIBLE else View.GONE
        binding.genderSpinner.visibility = if (edit) View.VISIBLE else View.GONE

        binding.name.inputType =
            if (edit) InputType.TYPE_TEXT_VARIATION_PERSON_NAME else InputType.TYPE_NULL
        binding.editBtn.text = getText(if (edit) R.string.update else R.string.edit)

        binding.sexualOrientationTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.agePreferenceTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.religionPreferenceTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.countryTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.stateTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.dobTxt.visibility = if (!edit) View.VISIBLE else View.GONE
        binding.genderTxt.visibility = if (!edit) View.VISIBLE else View.GONE
    }

    private fun setupView() {
        calendar = Calendar.getInstance()
        binding.dob.inputType = InputType.TYPE_NULL
        val date = OnDateSetListener { view, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, month)
            updateDateOfBirth()
        }

        binding.dob.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val videoUri = it.data?.data ?: return@registerForActivityResult
                    showLoader(true)
                    viewModel.uploadVideo(videoUri)
                }
            }
    }

    private fun updateDateOfBirth() {
        val dateStringFormat = "yyyy-MM-dd"
        val dateFormat = SimpleDateFormat(dateStringFormat, Locale.UK)
        binding.dob.setText(dateFormat.format(calendar.time))
        dob = binding.dob.text.toString()
    }

    private fun populateViews(user: User) {
        binding.name.setText(user.name)
        binding.countryTxt.text = user.country
        binding.stateTxt.text = user.state
        binding.religionPreferenceTxt.text = user.religion
        binding.agePreferenceTxt.text = user.agePreferred
        binding.sexualOrientationTxt.text = user.sexualOrientation

        viewModel.getStates(user.country)

        name = user.name
        country = user.country
        state = user.country
        gender = user.gender
        religionPreferred = user.religionPreferred
        agePreferred = user.agePreferred
        sexualOrientation = user.sexualOrientation
        dob = user.dob
    }

    private fun showLoader(show: Boolean) {
        binding.circularLyt.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.editBtn.visibility = if (show) View.GONE else View.VISIBLE
    }

    companion object {
        const val SEXUAL_ORIENTATION_SPINNER = 1
        const val AGE_GROUP_SPINNER = 2
        const val RELIGION_SPINNER = 3
        const val COUNTRY_SPINNER = 4
        const val STATE_SPINNER = 5
        const val GENDER_SPINNER = 6
        const val PERMISSION_REQUEST_CODE = 0
    }

}