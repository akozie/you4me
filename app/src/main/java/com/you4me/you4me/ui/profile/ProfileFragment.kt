package com.you4me.you4me.ui.profile

import android.Manifest
import android.app.*
import android.app.DatePickerDialog.OnDateSetListener
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.TokenRefreshReceiver
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.databinding.VideoDialogBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.authentication.AuthenticationActivity
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.profileDetails.ImageAndVideoDetailsActivity
import com.you4me.you4me.utils.*
import com.you4me.you4me.utils.SharedPrefHelper.Companion.COUNTRY_ID
import com.you4me.you4me.utils.SharedPrefHelper.Companion.PROFILE_IMAGE
import com.you4me.you4me.utils.Utils.BANNER_TIMEOUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ProfileFragment :
    BaseFragment<ProfileViewModel, FragmentProfileBinding, ProfileRepository>("PROFILE") {
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
    private lateinit var religions: ArrayList<ValueLabelResponse>
    private lateinit var sexualOrientations: ArrayList<ValueLabelResponse>
    private lateinit var calendar: Calendar
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var recordVideoLauncher: ActivityResultLauncher<Intent>
    private var videoUri: Uri? = null
    private lateinit var videoId: String
    private lateinit var imageID: String
    private var player: ExoPlayer? = null
    private lateinit var videoViewBinding: VideoDialogBinding
    private lateinit var updateBody: UpdateUserBody
    private val handler = Handler(Looper.getMainLooper())
//    private var imageDeletedReceiver: BroadcastReceiver =
//        object : BroadcastReceiver() {
//            override fun onReceive(
//                context: Context?,
//                intent: Intent?,
//            ) {
//                activity?.runOnUiThread {
//                    fetchImages()
//                }
//            }
//        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        Log.d("PROFILEID", userProfile)
        val gson = Gson()
        val newUser: User? = gson.fromJson(userProfile, User::class.java)
//        Log.d("OKKPROFILEID", newUser.toString())
        if (newUser != null) {
            user = newUser
//            populateViews(user)
        }
        observeUserDetails()
        addListeners()
        setupView()
        checkAndRequestPermissions()
//        observeImagesAndVideos()
        trackProfileViewed()
        getImages()
        setUpResultListener()

//        binding.boostLayout.setOnClickListener {
//            val action = ProfileFragmentDirections.actionProfileFragmentToProfileBoostFragment()
//            findNavController().navigate(action)
//            Log.d("YES_YES_TV", "YEAHHH")
//        }

        binding.deleteAccLyt.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_deleteAccountFragment)
        }

        binding.editBtn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
//            showLoader(true)
//
//
//             updateBody = UpdateUserBody(
//                agePreferred,
//                country,
//                dob,
//                gender,
//                binding.name.text.toString(),
//                 "",
////                binding.bio.text.toString(),
//                binding.completionPercentage.text.toString(),
//                religionPreferred,
//                sexualOrientation,
//                state,
//            )
//            if (viewModel.dbUser.value == null) {
//                viewModel._dbUser.value = user
//                viewModel.updateUserInfo(updateBody)
//            } else {
//                viewModel.updateUserInfo(updateBody)
//            }

        }


        mixpanel?.track("Android_Profile_Viewed")

        // Register the BroadcastReceiver
//        LocalBroadcastManager.getInstance(requireContext())
//            .registerReceiver(
//                imageDeletedReceiver,
//                IntentFilter("IMAGE_DELETED"),
//            )
//        addObservers()
    }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))


    private fun observeUserDetails() {
        viewModel.getUserDetails(user.userId)
        viewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    user = it.value
                    populateViews(user)
                }

                is Resource.Failure -> {
                }
            }
        }
        viewModel.getSubscriptionStatus(user.userId)
        viewModel.getSubscriptionStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val freeTrial = it.value.isFreeTrial
                    val premium = it.value.isPremium

                    if (freeTrial || premium){
                        binding.alreadySubscribed.isVisible = true
                        binding.notYetSubscribed.isVisible = false
                        binding.premiumImg.setImageResource(R.drawable.premium_img)
                    } else {
                        binding.alreadySubscribed.isVisible = false
                        binding.notYetSubscribed.isVisible = true
                        binding.premiumImg.setImageResource(R.drawable.subscription_card)
                    }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

    private fun addObservers() {
//        viewModel.ageGroups.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    agePreferences = it.value
//                    setupSpinner(it.value, AGE_GROUP_SPINNER)
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }
//        viewModel.religions.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    religions = it.value
//                    setupSpinner(it.value, RELIGION_PREFERENCE_SPINNER)
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }
//        viewModel.countries.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    countries = it.value
//                    setupSpinner(it.value, COUNTRY_SPINNER)
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }
//        viewModel.sexualOrientations.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    sexualOrientations = it.value
//                    setupSpinner(it.value, SEXUAL_ORIENTATION_SPINNER)
////                    Log.d("GENDERRRR", "${it.value[0].value}")
////                    binding.genderLabel.visibility =
////                        if (it.value[0].value == "4") View.VISIBLE else View.GONE
////                    binding.genderSpinner.visibility =
////                        if (it.value[0].value == "4") View.VISIBLE else View.GONE
////                    binding.divider8.visibility =
////                        if (it.value[0].value == "4") View.VISIBLE else View.GONE
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }
//        viewModel.states.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    states = it.value
//                    if (it.value.isNotEmpty()) setupSpinner(it.value, STATE_SPINNER)
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }
//        viewModel.genders.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    genders = it.value
//                    setupSpinner(it.value, GENDER_SPINNER)
//                }
//
//                is Resource.Failure -> {
//                }
//            }
//        }

        viewModel.validateVideoUpload.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    binding.videoBannerLayout.isVisible = true
//                    binding.profileLayout.isVisible = false
//                    showVideoRegulationsDialog()
                    openGallery()
                }

                is Resource.Failure -> {
                    showLoader(false)
                    if (it.errorCode == 400) {
                        showToast("You already uploaded a video")
                    } else {
                        showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
                    }
                }
            }
        }
        viewModel.registerVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(true)
                    viewModel.uploadVideo(videoUri!!, videoId)
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
        viewModel.uploadVideoCloudinaryResponse.observe(viewLifecycleOwner) {
            showLoader(true)
            viewModel.updateVideoUrl(it.public_id, it.url)
        }

        viewModel.uploadError.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showToast(it)
            }
        }


        viewModel.updateVideoUrlResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    mixpanel?.track("Android_Profile_Uploaded_Media")
                    showToast("Uploaded Successfully")
//                    videoViewBinding.videoView.setVideoURI(videoUri)
//                    initializePlayer()
                    showLoader(true)
                    binding.root.isVisible = true
                    binding.profileLayout.isVisible = true
                    getImages()
                }

                is Resource.Failure -> {}
            }
        }


    }

    private fun addListeners() {
//        binding.editBtn.setOnClickListener {
//            showLoader(true)
//            updateBody =
//                UpdateUserBody(
//                    agePreferred,
//                    country,
//                    dob,
//                    gender,
//                    binding.name.text.toString(),
//                    binding.bio.text.toString(),
//                    binding.completionPercentage.text.toString(),
//                    religionPreferred,
//                    sexualOrientation,
//                    state,
//                )
//            Log.d("THE_FIELDS", "$updateBody")

//            viewModel.updateUserInfo(
//                user,
//                updateBody,
//            )
//        }


//        binding.skipBanner.setOnClickListener {
//            binding.videoBannerLayout.isVisible = false
//        }

//        binding.frame1.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.frame2.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.frame3.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.frame4.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.frame5.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.frame6.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//
//        binding.stateSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        state = states[p2 - 1].value
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
//
//        binding.countrySpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        if (countries[p2 - 1].value != country) viewModel.getStates(countries[p2 - 1].value)
//                        country = countries[p2 - 1].value
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
//
//        binding.sexualOrientationSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        sexualOrientation = sexualOrientations[p2 - 1].value
////                        Log.d("GENDER", sexualOrientation.toString())
//                        // binding.genderLyt.visibility = if (sexualOrientation == "4") View.VISIBLE else View.GONE
//                        binding.genderLabel.visibility =
//                            if (sexualOrientation == "4") View.VISIBLE else View.GONE
//                        binding.genderSpinner.visibility =
//                            if (sexualOrientation == "4") View.VISIBLE else View.GONE
//                        binding.divider8.visibility =
//                            if (sexualOrientation == "4") View.VISIBLE else View.GONE
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
//
//        binding.religionPreferenceSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        religionPreferred = religions[p2 - 1].value
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
//
//        binding.agePreferenceSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        agePreferred = agePreferences[p2 - 1].value
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
//
//        binding.genderSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    p0: AdapterView<*>?,
//                    p1: View?,
//                    p2: Int,
//                    p3: Long,
//                ) {
//                    if (p2 == 0) return
//                    try {
//                        gender = genders[p2 - 1].value
//                    } catch (e: IndexOutOfBoundsException) {
//                        Log.e("SpinnerDebug", "Index out of bounds: ${e.message}")
//                    }
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                }
//            }
    }

    private fun setupSpinner(
        values: ArrayList<ValueLabelResponse>,
        spinner: Int,
    ) {
        val names =
            values.map {
                it.label
            }.toMutableList()
        names.add(0, "Select")

        ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_layout,
            names,
        ).also { adapter ->
            when (spinner) {
//                SEXUAL_ORIENTATION_SPINNER -> {
//                    binding.sexualOrientationSpinner.adapter = adapter
//                    binding.sexualOrientationSpinner.setSelection(sexualOrientations.indexOfFirst { it.value == sexualOrientation } + 1)
//                }
//
//                AGE_GROUP_SPINNER -> {
//                    binding.agePreferenceSpinner.adapter = adapter
//                    binding.agePreferenceSpinner.setSelection(agePreferences.indexOfFirst { it.value == agePreferred } + 1)
//                }
//
//                COUNTRY_SPINNER -> {
//                    binding.countrySpinner.adapter = adapter
//                    binding.countrySpinner.setSelection(countries.indexOfFirst { it.value == country } + 1)
//                }
//
////                This comment was left here by SEUN......
//
//                STATE_SPINNER -> {
//                    binding.stateSpinner.adapter = adapter
//                    binding.stateSpinner.setSelection(states.indexOfFirst { it.value == state } + 1)
//                }
//
//                GENDER_SPINNER -> {
//                    binding.genderSpinner.adapter = adapter
//                    binding.genderSpinner.setSelection(genders.indexOfFirst { it.value == gender } + 1)
//                }
//
//                RELIGION_PREFERENCE_SPINNER -> {
//                    binding.religionPreferenceSpinner.adapter = adapter
//                    binding.religionPreferenceSpinner.setSelection(religions.indexOfFirst { it.value == religionPreferred } + 1)
//                }
            }
        }
    }

    private fun setupView() {
        videoViewBinding = VideoDialogBinding.inflate(layoutInflater, null, false)
//        binding.btnPlay.visibility = View.GONE
//        binding.divider7.visibility = View.GONE
//        calendar = Calendar.getInstance()
//        binding.dob.inputType = InputType.TYPE_NULL
        val date =
            OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateOfBirth()
            }


//        binding.dob.setOnClickListener {
//            DatePickerDialog(
//                requireContext(),
//                date,
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH),
//            ).show()
//        }

//        binding.btnPlay.setOnClickListener {
////            if (videoUri == null) {
////                showToast("please upload a video")
////                return@setOnClickListener
////            } else {
//            showVideoDialog()
////            }
//        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    videoUri = it.data?.data ?: return@registerForActivityResult
                    if (validateMedia(videoUri!!, requireContext())) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)

                        viewModel.registerVideoUpload(
                            RegisterVideoUploadBody(
                                "$videoUri",
                                user.userId,
                                videoId,
                                getCategoryFromUri(requireContext(), videoUri!!),
                            ),
                        )
//                        Log.d("YEPAAA", "$videoUri")
                    } else {
                        showDialog("Video duration must not be longer than 30 seconds")
                    }
                }
            }

        recordVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the recorded video URI (e.g., upload it to your server or save it locally)
                    videoUri = result.data?.data ?: return@registerForActivityResult
                    if (validateMedia(videoUri!!, requireContext())) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)

                        viewModel.registerVideoUpload(
                            RegisterVideoUploadBody(
                                "$videoUri",
                                user.userId,
                                videoId,
                                getCategoryFromUri(requireContext(), videoUri!!),
                            ),
                        )
                    } else {
                        showDialog("Video duration must not be longer than 30 seconds")
                    }
                }
            }

        binding.logout.setOnClickListener {
            val alertDialog = AlertDialog.Builder(ctx)
            alertDialog.setTitle("Are you sure you want to Logout your account?")
            alertDialog.setPositiveButton("Cancel") { dialog, int ->
                dialog.dismiss()
            }
            alertDialog.setNegativeButton("Log out") { dialog, int ->
                viewModel.logout(user.userId)
                viewModel.logoutResponse.observe(viewLifecycleOwner) {
                    when (it) {
                        is Resource.Success -> {
                            trackProfileLogout()
                            showToast("Account logged out successfully!")
                            sharedPrefHelper.saveBoolean(SharedPrefHelper.IS_LOGGED_IN, false)
                            //stop refreshing token
                            stopRefreshingToken(requireContext())
                            // change shared pref to is logged out
                            val intent =
                                Intent(requireContext(), AuthenticationActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }

                        is Resource.Failure -> {
                            dialog.dismiss()
                            showAlertDialog(
                                requireContext(),
                                it.message ?: it.errorBody ?: "",
                                "OK",
                            ) {}
                        }
                    }
                }
            }
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
    }


    // Suspend function to generate video thumbnail in background
    private fun loadImagesAndVideosInBackground(listOfImagesAndVideos: ImagesVideosResponse) {


        val profileImageView = binding.profilePicture

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val profileImageItem = listOfImagesAndVideos.firstOrNull { it.isProfilePhoto }

            withContext(Dispatchers.Main) {
                // 🔵 Load profile image
                profileImageItem?.let {
                    Glide.with(profileImageView.context)
                        .load(it.fileURL)
                        .circleCrop()
                        .into(profileImageView)
                    sharedPrefHelper.saveString(PROFILE_IMAGE, it.videoId)
                }
            }
        }
    }

    private fun setUpResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "bottom_sheet_result",
            viewLifecycleOwner
        ) { _, result ->
            val value = result.getString("selected_value")
            val sheetId = result.getString("sheet_id")

            when (sheetId) {
                "EDIT_PROFILE" -> {
                    Glide.with(requireActivity()).load(value).into(binding.profilePicture)
                }
                // Add more as needed
            }
        }
    }



    private fun generateVideoThumbnail(videoUrl: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())
            retriever.frameAtTime
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

//    private fun openDetailScreen(
//        fileUrl: String,
//        category: String,
//        videoId: String,
//    ) {
//        val imagesVideosResponseItem =
//            ImagesVideosResponseItem(
//                category,
//                fileUrl,
//                "",
//                "",
//                "",
//                videoId,
//            )
//        val action = ProfileFragmentDirections.actionProfileFragmentToImageAndVideoDetailsFragment(imagesVideosResponseItem)
//        findNavController().navigate(action)
//    }

    private fun openDetailScreen(
        fileUrl: String,
        category: String,
        videoId: String
    ) {
        val intent =
            Intent(
                context,
                ImageAndVideoDetailsActivity::class.java,
            ).apply {
                putExtra("FILE_URL", fileUrl)
                putExtra("CATEGORY", category)
                putExtra("VIDEO_ID", videoId)
                putExtra("USER_ID", user.userId)
            }
        context?.startActivity(intent)
    }


    // Suspend function to generate video thumbnail in background

    private fun updateDateOfBirth() {
//        binding.dob.setText(Utils.getDateFormat().format(calendar.time))
//        dob = binding.dob.text.toString()
    }

    private fun populateViews(user: User) {
        Log.d("JUST_CHECKING", "$user")
        showLoader(false)
        if (user.bio.isEmpty()) {
            //
        } else {
            binding.bio.text = user.bio
        }
        binding.completionPercentage.text = "${user.completionPercentage}%"
        binding.name.text = user.name
        if(user.isVerified){
            binding.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.verified, 0)
        }else{
            binding.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
        name = user.name
        country = user.country
        state = user.state
        gender = user.gender
        religionPreferred = user.religionPreferred
        agePreferred = user.agePreferred
        sexualOrientation = user.sexualOrientation
        dob = user.dob
//        addObservers()
    }

    private fun showLoader(show: Boolean) {
        if (show) activity?.showSimpleProgressDialog() else removeSimpleProgressDialog()
//        binding.editBtn.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun checkVideoDuration(uri: Uri): Boolean {
        val mp: MediaPlayer = MediaPlayer.create(ctx, uri)
        val duration = mp.duration.toLong()
        mp.release()
        return duration <= 30000
    }



    private fun setupVideo() {
        videoUri = Uri.parse(user.videoURL.replace("http:", "https:"))
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun showVideoRegulationsDialog() {
        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Upload Requirements and Regulations")
        builder.setMessage(getString(R.string.video_regulations))
        builder.setPositiveButton("Select Video") { d, i ->
//            showVideoOptionsDialog()
            openGallery()
            showLoader(false)
        }
        builder.setNegativeButton("Cancel") { d, i ->
            showLoader(false)
            d.dismiss()
        }

        handler.postDelayed({
//            binding.videoBannerLayout.isVisible = false
            binding.profileLayout.isVisible = true
            builder.show()
        }, BANNER_TIMEOUT) // 5000 milliseconds = 5 seconds
    }

    private fun openGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Files.getContentUri("external")).apply {
                type = "*/*" // Allow all media types
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf("image/*", "video/*"),
                ) // Filter for images and videos
            }
        activityResultLauncher.launch(intent)
    }

    private fun showVideoDialog() {
        val dialog = Dialog(ctx)
        if (videoViewBinding.root.parent != null) {
            (videoViewBinding.root.parent as ViewGroup).removeView(videoViewBinding.root)
        }
        dialog.setContentView(videoViewBinding.root)
        dialog.show()
        player?.play()
    }

//    override fun onResume() {
//        super.onResume()
//        fetchImages()
//    }

    private fun fetchImages() {
        showLoader(true)
        getImages()
    }

    private fun getImages() {
        showLoader(true)
        viewModel.getImagesAndVideos(user.userId)
        viewModel.getImagesAndVideos.observe(viewLifecycleOwner) { images ->
            when (images) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = images.value
                    try {
                        showLoader(false)
                        // Your potentially crashing code (e.g., loading images, videos, etc.)
                        if (isAdded() && getActivity() != null) {
                            // Perform operations safely
                            loadImagesAndVideosInBackground(listOfImagesAndVideos)
                        }
                    } catch (e: Exception) {
                        Log.e("MyApp", "Error loading data", e)
                    }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun initializePlayer() {
        if (videoUri == null) return
        if (player != null) player = null
        player =
            ExoPlayer.Builder(ctx).build().also {
                videoViewBinding.videoView.player = it
                val mediaItem = MediaItem.fromUri(videoUri!!)
                it.setMediaItem(mediaItem)
                it.prepare()
            }
    }

    private val permissions =
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
        )

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (permissions.any {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        it,
                    ) != PackageManager.PERMISSION_GRANTED
                }
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    permissions,
                    PERMISSION_REQUEST_CODE,
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE,
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions are required to access media files.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }


    private fun stopRefreshingToken(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TokenRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

    }

    private fun trackProfileViewed() {
        mixpanel?.track("Android_Profile_Viewed")
    }

    private fun trackProfileUpdate() {
        mixpanel?.track("Android_Profile_Update_Button_Clicked")
    }

    private fun trackProfileDeleted() {
        mixpanel?.track("Android_Profile_Delete_Button_Clicked")
    }

    private fun trackProfileLogout() {
        mixpanel?.track("Android_Profile_Logout_Button_Clicked")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)

        // Unregister the receiver to avoid memory leaks
//        LocalBroadcastManager.getInstance(requireContext())
//            .unregisterReceiver(imageDeletedReceiver)
    }

    companion object {
        const val SEXUAL_ORIENTATION_SPINNER = 1
        const val AGE_GROUP_SPINNER = 2
        const val COUNTRY_SPINNER = 4
        const val STATE_SPINNER = 5
        const val GENDER_SPINNER = 6
        const val RELIGION_PREFERENCE_SPINNER = 7
        const val PERMISSION_REQUEST_CODE = 1001
    }
}
