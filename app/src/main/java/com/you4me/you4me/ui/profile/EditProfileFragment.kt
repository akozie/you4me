package com.you4me.you4me.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentEditProfileBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.models.useroptions.AgeGroup
import com.you4me.you4me.models.useroptions.Gender
import com.you4me.you4me.models.useroptions.Religion
import com.you4me.you4me.models.useroptions.SexualOrientation
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.verification.VeriffActivity
import com.you4me.you4me.utils.*
import com.you4me.you4me.utils.SharedPrefHelper.Companion.COUNTRY_ID
import com.you4me.you4me.utils.SharedPrefHelper.Companion.SESSION_ID
import com.you4me.you4me.utils.SharedPrefHelper.Companion.SEXUALITY_ID
import com.you4me.you4me.utils.SharedPrefHelper.Companion.USERNAME
import com.you4me.you4me.utils.Utils.generateVideoThumbnail
import com.you4me.you4me.utils.Utils.getCategoryFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class EditProfileFragment :
    BaseFragment<ProfileViewModel, FragmentEditProfileBinding, ProfileRepository>("EDIT_PROFILE") {

    private lateinit var dob: String
    private lateinit var calendar: Calendar
    private lateinit var name: String
    private lateinit var agePreferred: String
    private lateinit var gender: String
    private lateinit var religionPreferred: String
    private lateinit var sexualOrientation: String
    private lateinit var state: String
    private lateinit var country: String
    private lateinit var user: User
    private lateinit var ageGroup: List<AgeGroup>
    private lateinit var newGender: List<Gender>
    private lateinit var religion: List<Religion>
    private lateinit var sexualOrientations: List<SexualOrientation>
    private lateinit var countries: ArrayList<ValueLabelResponse>
    private lateinit var states: ArrayList<ValueLabelResponse>
    private lateinit var updateBody: UpdateUserBody
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var recordVideoLauncher: ActivityResultLauncher<Intent>
    private var videoUri: Uri? = null
    private lateinit var videoId: String
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var image1 : ImageView
    private lateinit var addBadge1 : ImageView
    private lateinit var cancel1 : ImageView

    private val imageIds = listOf(
        R.id.image1,
        R.id.image2,
        R.id.image3,
        R.id.image4,
        R.id.image5,
        R.id.image6 /* add image3..image6 IDs */
    )

    private var imageDeletedReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                activity?.runOnUiThread {
                    fetchImages()
                }
            }
        }

    override fun getViewModel() = ProfileViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentEditProfileBinding.inflate(inflater, container, false)

    override fun getRepository() = ProfileRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        val newUser: User? = gson.fromJson(userProfile, User::class.java)
        if (newUser != null) {
            user = newUser
        }

        image1 = binding.image1
        addBadge1 = binding.addBadge1
        cancel1 = binding.cancel1

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


        viewModel.getUserOptionsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    ageGroup = it.value.ageGroups
                    newGender = it.value.genders
                    religion = it.value.religions
                    sexualOrientations = it.value.sexualOrientations
                }

                is Resource.Failure -> {
                }
            }
        }

        viewModel.countries.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    countries = it.value
                }

                is Resource.Failure -> {
                }
            }
        }
        viewModel.states.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    states = it.value
                }

                is Resource.Failure -> {
                }
            }
        }

        binding.name.setOnClickListener {
            val userName = binding.name.text.toString().trim()
            sharedPrefHelper.saveString(USERNAME, userName)
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToUserNameBottomSheetFragment(
                    userName
                )
            findNavController().navigate(action)
            Log.d("PROFILE_TEXT", userName)
        }

        binding.sexuality.setOnClickListener {
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToSexualityBottomSheetFragment(
                    sexualOrientations.toTypedArray()
                )
            findNavController().navigate(action)
        }

        binding.gender.setOnClickListener {
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToGenderBottomSheetFragment(
                    newGender.toTypedArray()
                )
            findNavController().navigate(action)
        }

        binding.religionPreference.setOnClickListener {
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToReligionBottomSheetFragment(
                    religion.toTypedArray()
                )
            findNavController().navigate(action)
        }

        binding.agePreference.setOnClickListener {
            if (ageGroup.isNotEmpty()) {
                val newAgeGroup = ageGroup.toTypedArray()
                val action =
                    EditProfileFragmentDirections.actionEditProfileFragmentToAgePreferenceBottomSheetFragment(
                        newAgeGroup
                    )
                findNavController().navigate(action)
            }
        }

        binding.country.setOnClickListener {
            val action =
                EditProfileFragmentDirections.actionEditProfileFragmentToCountryBottomSheetFragment(
                    countries.toTypedArray()
                )
            findNavController().navigate(action)
        }

        binding.state.setOnClickListener {
            if (::states.isInitialized) {
                val action =
                    EditProfileFragmentDirections.actionEditProfileFragmentToStateBottomSheetFragment(
                        states.toTypedArray()
                    )
                findNavController().navigate(action)
            } else {
                showToast("Please select a country")
            }
        }


        binding.dob.setOnClickListener {
            calendar = Calendar.getInstance()
            binding.dob.inputType = InputType.TYPE_NULL

            setDob()
        }

        binding.bio.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_bioBottomSheetFragment)
        }

        binding.uploadProfilePictureTxt.setOnClickListener {
            findNavController().navigate(R.id.action_editProfileFragment_to_editProfilePictureBottomSheetFragment)
        }

        binding.verifyYourIdentity.setOnClickListener {
            showCustomDialog(requireContext())

        }

        observeUserDetails()
        addListeners()
        addObservers()
        setUpResultListener()


        for (id in imageIds) {
            val imageView = view.findViewById<ImageView>(id)

            // Start dragging
            imageView.setOnLongClickListener { view ->
                val dragData = ClipData.newPlainText("", "")
                val shadow = View.DragShadowBuilder(view)
                view.startDragAndDrop(dragData, shadow, view, 0)
                true
            }

            // Handle drop
            imageView.setOnDragListener { targetView, event ->
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        val draggedView = event.localState as ImageView
                        if (draggedView != targetView) {
                            // Swap image drawables
                            val draggedDrawable = draggedView.drawable
                            val targetDrawable = (targetView as ImageView).drawable

                            draggedView.setImageDrawable(targetDrawable)
                            targetView.setImageDrawable(draggedDrawable)

                            // Optional: swap tags too (for future tracking)
                            val tempTag = draggedView.tag
                            draggedView.tag = targetView.tag
                            targetView.tag = tempTag
                        }
                        true
                    }

                    DragEvent.ACTION_DRAG_ENDED -> {
                        // Restore visibility in case it was hidden
                        val draggedView = event.localState as ImageView
                        draggedView.visibility = View.VISIBLE
                        true
                    }

                    DragEvent.ACTION_DRAG_STARTED,
                    DragEvent.ACTION_DRAG_ENTERED,
                    DragEvent.ACTION_DRAG_EXITED,
                    DragEvent.ACTION_DRAG_LOCATION -> true

                    else -> false
                }
            }
        }


        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(
                imageDeletedReceiver,
                IntentFilter("IMAGE_DELETED"),
            )
    }

    private fun showCustomDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_start_verification, null)

        val btnAction = dialogView.findViewById<LinearLayout>(R.id.save_btn)
        val closeDialog = dialogView.findViewById<ImageView>(R.id.cancel_icon)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        btnAction.setOnClickListener {
            showLoader(true)
            veriffVerification()
            dialog.dismiss()
        }
        closeDialog.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addObservers() {
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

        override fun onResume() {
        super.onResume()
        fetchImages()
    }

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

    private fun loadImagesAndVideosInBackground(listOfImagesAndVideos: ImagesVideosResponse) {
        val imageSlots = listOf(
            ImageSlotViews(binding.image1, binding.addBadge1, binding.cancel1),
            ImageSlotViews(binding.image2, binding.addBadge2, binding.cancel2),
            ImageSlotViews(binding.image3, binding.addBadge3, binding.cancel3),
            ImageSlotViews(binding.image4, binding.addBadge4, binding.cancel4),
            ImageSlotViews(binding.image5, binding.addBadge5, binding.cancel5),
            ImageSlotViews(binding.image6, binding.addBadge6, binding.cancel6)
        )

        val profileImageView = binding.profilePicture

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val profileImageItem = listOfImagesAndVideos.firstOrNull { it.isProfilePhoto }
            val galleryItems = listOfImagesAndVideos.filterNot { it.isProfilePhoto }.take(imageSlots.size)

            withContext(Dispatchers.Main) {
                // 🔵 Load profile image
                profileImageItem?.let {
                    Glide.with(profileImageView.context)
                        .load(it.fileURL)
                        .circleCrop()
                        .into(profileImageView)
                    binding.uploadProfilePictureTxt.text = "Edit Profile Picture"
                }

                // 🟢 Load the rest into the 6 slots
                imageSlots.forEachIndexed { index, slot ->
                    val item = galleryItems.getOrNull(index)

                    if (item != null && item.fileURL.isNotBlank()) {
                        Glide.with(slot.imageView.context)
                            .load(item.fileURL)
                            .centerCrop()
                            .into(slot.imageView)

                        slot.imageView.visibility = View.VISIBLE
                        slot.imageView.clipToOutline = true
                        slot.addBadge.visibility = View.INVISIBLE
                        slot.cancelButton.visibility = View.VISIBLE
                    } else {
                        slot.imageView.setImageDrawable(null)
                        slot.imageView.visibility = View.INVISIBLE
                        slot.addBadge.visibility = View.VISIBLE
                        slot.cancelButton.visibility = View.GONE
                    }

                    // 🔴 Pass image info to delete function
                    slot.cancelButton.setOnClickListener {
                        item?.let { imageItem ->
                            deleteImageById(imageItem.videoId) {
                                // ✅ Only clear slot UI after successful API response
                                slot.imageView.setImageDrawable(null)
                                slot.imageView.visibility = View.INVISIBLE
                                slot.addBadge.visibility = View.VISIBLE
                                slot.cancelButton.visibility = View.GONE
                            }
                        }
                    }


                    slot.addBadge.setOnClickListener {
                        viewModel.validateVideoUpload()
                    }
                }
            }
        }
    }


    private fun deleteImageById(videoId: String, onSuccess: () -> Unit){
            showLoader(true)
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setMessage("Do you want to delete this image?")
            alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                showLoader(false)
                dialog.dismiss()
            }
            alertDialog.setPositiveButton("Delete") { _, _ ->
                viewModel.deleteVideoUpload(videoId ?: "")
                observeDeleteVideo(user.userId ?: "", requireActivity(), onSuccess)
            }
            alertDialog.setCancelable(false)
            alertDialog.show()

    }

    private fun observeDeleteVideo(userId: String, context: Context, onSuccess: () -> Unit) {
        viewModel.deleteVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(false)
                    showToast("Deleted Successfully")
                    viewModel.getImagesAndVideos(userId)
                    // Send broadcast to update ProfileFragment
                    val intent = Intent("IMAGE_DELETED")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    onSuccess()
//                    finish() // Close activity after deletion
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
    }

    private fun openDetailScreen(
        fileUrl: String,
        category: String,
        videoId: String,
    ) {
        val imagesVideosResponseItem =
            ImagesVideosResponseItem(
                category,
                fileUrl,
                "",
                "",
                "",
                videoId,
                false
            )
        val action = EditProfileFragmentDirections.actionEditProfileFragmentToImageAndVideoDetailsFragment(imagesVideosResponseItem)
        findNavController().navigate(action)
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


    private fun veriffVerification() {
        viewModel.requestVerification(user.userId)
        viewModel.requestVeriffVerification.observe(viewLifecycleOwner) {
            showLoader(false)
            when (it) {
                is Resource.Success -> {
//                    Log.d("HEYYY", "THANK YOU")
                    val sessionId = it.value.verification.url
                    val intent = Intent(requireActivity(), VeriffActivity::class.java)
                    intent.putExtra(SESSION_ID, sessionId)
                    startActivity(intent)
                }

                is Resource.Failure -> {
                    Log.d("HEYYY", "THANKS")

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
                "AGE" -> {
                    // Store or update age value
                    binding.agePreference.text = value
                }
                "NAME" -> {
                    binding.name.text = value
                }
                "COUNTRY" -> {
                    binding.country.text = value
                    val countryId = sharedPrefHelper.getString(COUNTRY_ID)
                    viewModel.getStates(countryId)
                }
                "SEXUALITY" -> {
                    binding.sexuality.text = value
                    val sexualityId = sharedPrefHelper.getString(SEXUALITY_ID)
                    binding.genderConstraintLayout.visibility =
                        if (sexualityId == "4") View.VISIBLE else View.GONE
//
                }
                "BIO" -> {
                    binding.bio.text = value
                }
                "STATE" -> {
                    binding.state.text = value
                }
                "GENDER" -> {
                    binding.gender.text = value
                }
                "RELIGION" -> {
                    binding.religionPreference.text = value
                }
                // Add more as needed
            }
        }
    }

    private fun observeUserDetails() {
        viewModel.getUserDetails(user.userId)
        viewModel.user.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    user = it.value
//                    Log.d("CHECKING_USER", "$user")
                    populateViews(user)
//                    if (it.value.videoURL.isNotBlank()) {
//                        binding.btnPlay.visibility = View.VISIBLE
//                        binding.divider7.visibility = View.VISIBLE
//                        setupVideo()
//                    }
                    binding.updateProfileBtn.text =
                        if (it.value.status.contains("incomplete")) {
                            getString(R.string.upload)
                        } else {
                            getString(
                                R.string.update,
                            )
                        }
//                    binding.addVideoLyt.text =
//                        if (it.value.status.contains("incomplete")) {
//                            getString(R.string.upload_video)
//                        } else {
//                            getString(
//                                R.string.update_video,
//                            )
//                        }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

    private fun setDob() {
        val date =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateOfBirth()
            }

        DatePickerDialog(
            requireContext(),
            date,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        ).show()

    }

    private fun populateViews(user: User) {
        Log.d("JUST_CHECKING_EDITPROFILE", "$user")
//        showLoader(false)
        binding.name.setText(user.name)
        if (user.bio.isEmpty()) {
            //
        } else {
            binding.bio.setText(user.bio)
        }
        binding.completionPercentage.text = "${user.completionPercentage}%"

        binding.dob.setText(user.dob)
        val countryId = sharedPrefHelper.getString(COUNTRY_ID)
        viewModel.getStates(countryId)

        binding.genderConstraintLayout.visibility =
            if (user.sexualOrientation == "Straight") View.VISIBLE else View.GONE

        binding.sexuality.text = user.sexualOrientation
        binding.gender.text = user.gender
        binding.agePreference.text = user.agePreferred
        binding.religionPreference.text = user.religion
        binding.country.text = user.country
        binding.state.text = user.state

//        name = user.name
//        country = user.country
//        state = user.state
//        gender = user.gender
//        religionPreferred = user.religionPreferred
//        agePreferred = user.agePreferred
//        sexualOrientation = user.sexualOrientation
//        dob = user.dob

    }


    private fun updateDateOfBirth() {
        binding.dob.setText(Utils.getDateFormat().format(calendar.time))
        dob = binding.dob.text.toString()
    }


    private fun addListeners() {
        binding.updateProfileBtn.setOnClickListener {
            if (binding.agePreference.text.toString()
                    .contains("Nil") || binding.country.text.toString().contains("Nil") ||
                binding.dob.text.toString().contains("Nil") || binding.gender.text.toString()
                    .contains("Nil") ||
                binding.name.text.toString().contains("Nil") || binding.bio.text.toString()
                    .contains("Nil") ||
                binding.religionPreference.text.toString()
                    .contains("Nil") || binding.sexuality.text.toString().contains("Nil") ||
                binding.state.text.toString().contains("Nil") || binding.country.text.toString()
                    .contains("Nil")
            ) {
                showToast("All fields are required")
            } else {
                showLoader(true)
                updateBody =
                    UpdateUserBody(
                        binding.agePreference.text.toString(),
                        binding.country.text.toString(),
                        binding.dob.text.toString(),
                        binding.gender.text.toString(),
                        binding.name.text.toString(),
                        binding.bio.text.toString(),
                        binding.completionPercentage.text.toString(),
                        binding.religionPreference.text.toString(),
                        binding.sexuality.text.toString(),
                        binding.state.text.toString(),
                    )
                Log.d("THE_FIELDS", "$updateBody")

                viewModel.updateUserInfo(
                    updateBody,
                    user.userId
                )
                viewModel.updateUserResponse.observe(viewLifecycleOwner) {
                    showLoader(false)
                    when (it) {
                        is Resource.Success -> {
                            viewModel.getUserProfileDetails(user.userId)
                            viewModel.userDetails.removeObservers(viewLifecycleOwner)
                            viewModel.userDetails.observe(viewLifecycleOwner) { users ->
                                when (users) {
                                    is Resource.Success -> {
                                        user = users.value
                                        val gson = Gson()
                                        val userProfileJsonString = gson.toJson(user)
                                        sharedPrefHelper.saveString(
                                            SharedPrefHelper.USER_PROFILE,
                                            userProfileJsonString
                                        )
                                        viewModel.updateUser(updateBody)
                                        observeUserDetails()
                                        if (user.completionPercentage.contains("100")) {
                                            val message =
                                                "Profile Update Successful, you can now create dates"
                                            showAlertDialog(requireContext(), message, "OK") {
                                                val navOptions = NavOptions.Builder()
                                                    .setPopUpTo(
                                                        R.id.homeFragment,
                                                        false
                                                    )  // Clears backstack up to homeFragment
                                                    .build()

                                                findNavController().navigate(
                                                    R.id.goOnDateFragment,
                                                    null,
                                                    navOptions
                                                )
                                            }
                                        } else {
                                            val message = "Profile Update Successful"
                                            showAlertDialog(requireContext(), message, "OK") {}

                                        }
                                    }

                                    is Resource.Failure -> {
                                    }
                                }
                            }

                            trackProfileUpdate()
                        }

                        is Resource.Failure -> {
                            showAlertDialog(
                                requireContext(),
                                it.message ?: it.errorBody ?: "",
                                "OK"
                            ) {}
                        }
                    }
                }

            }

        }


        binding.skipBanner.setOnClickListener {
            binding.videoBannerLayout.isVisible = false
        }

//        binding.addPhotoButton1.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.addPhotoButton2.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.addPhotoButton3.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.addPhotoButton4.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.addPhotoButton5.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }
//        binding.addPhotoButton6.setOnClickListener {
//            showLoader(true)
//            viewModel.validateVideoUpload()
//        }

//        addBadge1.setOnClickListener {
//            // Show image picker, assume you get 'bitmap'
////            image1.setImageBitmap(bitmap)
//            image1.visibility = View.VISIBLE
//            addBadge1.visibility = View.GONE
//            cancel1.visibility = View.VISIBLE
//            viewModel.validateVideoUpload()
//        }
//
//        cancel1.setOnClickListener {
//            image1.setImageDrawable(null)
//            image1.visibility = View.INVISIBLE
//            addBadge1.visibility = View.VISIBLE
//            cancel1.visibility = View.GONE
//        }
    }

    private fun showLoader(show: Boolean) {
        if (show) activity?.showSimpleProgressDialog() else removeSimpleProgressDialog()
//        binding.editBtn.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun trackProfileUpdate() {
        mixpanel?.track("Android_Edit_Profile_Update_Button_Clicked")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks to prevent memory leaks
        handler.removeCallbacksAndMessages(null)

        // Unregister the receiver to avoid memory leaks
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(imageDeletedReceiver)
    }
}