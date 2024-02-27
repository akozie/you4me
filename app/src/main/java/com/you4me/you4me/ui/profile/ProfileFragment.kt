package com.you4me.you4me.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

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
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var videoUri: Uri
    private lateinit var videoId: String

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

        viewModel.ageGroups.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    agePreferences = it.value
                    setupSpinner(it.value, AGE_GROUP_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.religions.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    religiousPreferences = it.value
                    setupSpinner(it.value, RELIGION_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.countries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    countries = it.value
                    setupSpinner(it.value, COUNTRY_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    sexualOrientations = it.value
                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.states.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    states = it.value
                    if (it.value.isNotEmpty()) setupSpinner(it.value, STATE_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.genders.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    genders = it.value
                    setupSpinner(it.value, GENDER_SPINNER)
                }

                is Resource.Failure -> {

                }
            }
        }
        viewModel.updateUserResponse.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Profile Update Successful")
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "An error occurred")
                }
            }
        }
        viewModel.validateVideoUpload.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intent)
                }

                is Resource.Failure -> {
                    showLoader(false)
                    if (it.errorCode == 400) {
                        showToast("You already uploaded a video")
                    } else {
                        showDialog(it.message ?: it.errorBody ?: "An error occurred")
                    }
                }
            }
        }
        viewModel.registerVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    viewModel.uploadVideo(videoUri, videoId)
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
        viewModel.uploadVideoCloudinaryResponse.observe(viewLifecycleOwner) {
            viewModel.updateVideoUrl(it.public_id, it.url)
        }
        viewModel.updateVideoUrlResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(false)
                    showToast("Video Upload Successful!")
                }

                is Resource.Failure -> {}
            }
        }
    }

    private fun addListeners() {
        binding.editBtn.setOnClickListener {
//            when (binding.editBtn.text) {
//                getText(R.string.update) -> {
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

        binding.addVideoLyt.setOnClickListener {
            showLoader(true)
            viewModel.validateVideoUpload()
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
                    binding.genderLyt.visibility = if (sexualOrientation == "4") View.VISIBLE
                    else View.GONE
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
                SEXUAL_ORIENTATION_SPINNER -> {
                    binding.sexualOrientationSpinner.adapter = adapter
                    binding.sexualOrientationSpinner.setSelection(sexualOrientations.indexOfFirst { it.value == sexualOrientation } + 1)
                }

                AGE_GROUP_SPINNER -> {
                    binding.agePreferenceSpinner.adapter = adapter
                    binding.agePreferenceSpinner.setSelection(agePreferences.indexOfFirst { it.value == agePreferred } + 1)
                }

                RELIGION_SPINNER -> {
                    binding.religionPreferenceSpinner.adapter = adapter
                    binding.religionPreferenceSpinner.setSelection(religiousPreferences.indexOfFirst { it.value == religionPreferred } + 1)
                }

                COUNTRY_SPINNER -> {
                    binding.countrySpinner.adapter = adapter
                    binding.countrySpinner.setSelection(countries.indexOfFirst { it.value == country } + 1)
                }

//                This comment was left here by SEUN......

                STATE_SPINNER -> {
                    binding.stateSpinner.adapter = adapter
                    binding.stateSpinner.setSelection(states.indexOfFirst { it.value == state } + 1)
                }

                GENDER_SPINNER -> {
                    binding.genderSpinner.adapter = adapter
                    binding.genderSpinner.setSelection(genders.indexOfFirst { it.value == gender } + 1)
                }
            }
        }
    }

    private fun setupView() {
        dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.UK)
        calendar = Calendar.getInstance()
        binding.dob.inputType = InputType.TYPE_NULL
        val date = OnDateSetListener { _, year, month, _ ->
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
                    videoUri = it.data?.data ?: return@registerForActivityResult
                    videoId = UUID.randomUUID().toString()
                    showLoader(true)

                    viewModel.registerVideoUpload(
                        RegisterVideoUploadBody(
                            "",
                            user.userId,
                            videoId
                        )
                    )
                }
            }
    }

    private fun updateDateOfBirth() {
        binding.dob.setText(dateFormat.format(calendar.time))
        dob = binding.dob.text.toString()
    }

    private fun populateViews(user: User) {
        binding.name.setText(user.name)
        binding.dob.setText(user.dob)

        viewModel.getStates(user.country)
        binding.genderLyt.visibility = if (user.sexualOrientation == "4") View.VISIBLE
        else View.GONE

        name = user.name
        country = user.country
        state = user.state
        gender = user.gender
        religionPreferred = user.religionPreferred
        agePreferred = user.agePreferred
        sexualOrientation = user.sexualOrientation
        dob = user.dob
    }

    private fun showLoader(show: Boolean) {
        binding.progressCircular.visibility = if (show) View.VISIBLE else View.GONE
        binding.editBtn.visibility = if (show) View.GONE else View.VISIBLE
    }

    companion object {
        const val SEXUAL_ORIENTATION_SPINNER = 1
        const val AGE_GROUP_SPINNER = 2
        const val RELIGION_SPINNER = 3
        const val COUNTRY_SPINNER = 4
        const val STATE_SPINNER = 5
        const val GENDER_SPINNER = 6
    }

}