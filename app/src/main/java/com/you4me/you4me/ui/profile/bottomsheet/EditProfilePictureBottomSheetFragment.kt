package com.you4me.you4me.ui.profile.bottomsheet

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.you4me.you4me.core.AppDatabase
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.databinding.FragmentEditProfilePictureBottomSheetBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.ImagesVideosResponse
import com.you4me.you4me.models.RegisterProfilePhotoBody
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.profile.ProfileViewModel
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.SharedPrefHelper.Companion.PROFILE_IMAGE
import com.you4me.you4me.utils.Utils.showAlertDialog
import com.you4me.you4me.utils.removeSimpleProgressDialog
import com.you4me.you4me.utils.showSimpleProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class EditProfilePictureBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentEditProfilePictureBottomSheetBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var activityResultLauncherForCamera: ActivityResultLauncher<Intent>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    //    private var videoUri: Uri? = null
    private lateinit var videoId: String
    private lateinit var user: User
    private lateinit var photoUri: Uri

    private var player: ExoPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var imageDeletedReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                activity?.runOnUiThread {
//                    fetchImages()
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentEditProfilePictureBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        isCancelable = false

        activityResultLauncherForCamera =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Log.d("YEPAAAKK", "Captured image URI: $photoUri")

                    if (photoUri != null && validateMedia(photoUri!!)) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)

                        viewModel.registerProfilePhotoUpload(
                            RegisterProfilePhotoBody(
                                "$photoUri",
                                user.userId,
                                videoId,
                                getCategoryFromUri(requireContext(), photoUri!!),
                                isProfilePhoto = true,
                            ),
                        )
                    } else {
                        // showDialog("Media is not valid")
                    }
                }
            }


        repository =
            ProfileRepository(
                RemoteDataSource().buildApi(
                    ApiCollector::class.java,
                ),
            )
        viewModel =
            ProfileViewModel(
                repository,
                DbRepository(
                    AppDatabase.invoke(requireContext()),
                ),
            )

        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        val newUser: User? = gson.fromJson(userProfile, User::class.java)
        if (newUser != null) {
            user = newUser
//            populateViews(user)
        }

        binding.takePhoto.setOnClickListener {
            showLoader(true)
//            viewModel.cameraUpload()
            openCamera()
        }
        binding.chooseFromLibrary.setOnClickListener {
            showLoader(true)
//            viewModel.validateVideoUpload()
            openGallery()
        }
        binding.cancelIcon.setOnClickListener {
            dismiss()
        }
        setUpView()
        addObservers()

    }

    private fun setUpView() {


        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    photoUri = it.data?.data ?: return@registerForActivityResult
                    if (com.you4me.you4me.utils.validateMedia(photoUri!!, requireContext())) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)
                        viewModel.registerVideoUpload(
                            RegisterVideoUploadBody(
                                "$photoUri",
                                user.userId,
                                videoId,
                                com.you4me.you4me.utils.getCategoryFromUri(
                                    requireContext(),
                                    photoUri!!
                                ),
                            ),
                        )
//                        Log.d("YEPAAA", "$videoUri")
                    } else {
//                        showDialog("Video duration must not be longer than 30 seconds")
                    }
                }
            }


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

    private fun openCamera() {
        val photoFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }


        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activityResultLauncherForCamera.launch(intent)
    }

    fun showToast(
        message: String,
        length: Int = Toast.LENGTH_SHORT,
    ) {
        Toast.makeText(requireContext(), message, length).show()
    }

    private fun addObservers() {

        viewModel.cameraUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    openCamera()
                }

                is Resource.Failure -> {
                    showLoader(false)
                    if (it.errorCode == 400) {
                        showToast("You already uploaded a video")
                    } else {
                        showAlertDialog(
                            requireContext(),
                            it.message ?: it.errorBody ?: "",
                            "OK",
                            "",
                            {}) {}
                    }
                }
            }
        }
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
                        showAlertDialog(
                            requireContext(),
                            it.message ?: it.errorBody ?: "",
                            "OK",
                            "",
                            {}) {}
                    }
                }
            }
        }
        viewModel.registerPhotoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(true)
                    viewModel.uploadVideo(photoUri!!, videoId)
                }

                is Resource.Failure -> {
                    showLoader(false)
                }
            }
        }
        viewModel.registerVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(true)
                    viewModel.uploadVideo(photoUri!!, videoId)
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
                    val imageId = sharedPrefHelper.getString(PROFILE_IMAGE)
//                    mixpanel?.track("Android_Profile_Uploaded_Media")
                    showToast("Uploaded Successfully")
//                    videoViewBinding.videoView.setVideoURI(videoUri)
//                    initializePlayer()
                    showLoader(true)
                    binding.root.isVisible = true
                    viewModel.deleteVideoUpload(imageId)
                    observeDeleteVideo()
                    val result = Bundle().apply {
                        putString("selected_value", "$photoUri")
                        putString("sheet_id", "EDIT_PROFILE")  // Unique tag for the sheet
                    }
                    setFragmentResult("bottom_sheet_result", result)
                }

                is Resource.Failure -> {}
            }
        }
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

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val profileImageItem = listOfImagesAndVideos.firstOrNull { it.isProfilePhoto }

            withContext(Dispatchers.Main) {
                // 🔵 Load profile image
                profileImageItem?.let {
                    sharedPrefHelper.saveString(PROFILE_IMAGE, it.videoId)
                    dismiss()
                }

            }
        }
    }

    private fun observeDeleteVideo() {
        viewModel.deleteVideoUploadResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showLoader(false)
                    getImages()
                }

                is Resource.Failure -> {
                    showLoader(false)
                    dismiss()
                }
            }
        }
    }

    private fun validateMedia(uri: Uri): Boolean {
        val contentResolver = requireActivity().contentResolver
        val mimeType = contentResolver.getType(uri)

        return if (mimeType?.startsWith("video/") == true) {
            // Check video duration
            val mediaPlayer = MediaPlayer.create(requireContext(), uri)
            val duration = mediaPlayer?.duration?.toLong() ?: 0
            mediaPlayer?.release()
            duration <= 30000 // Validate that video duration is <= 30 seconds
        } else if (mimeType?.startsWith("image/") == true) {
            // Example validation for images (optional, can customize based on your requirements)
            true // Allow all images
        } else {
            false // Unsupported type
        }
    }

    fun getCategoryFromUri(
        context: Context,
        fileUri: Uri,
    ): String {
        val contentResolver: ContentResolver = context.contentResolver
        val mimeType = contentResolver.getType(fileUri) // Get the MIME type of the file

        return if (mimeType?.startsWith("image") == true) {
            "image"
        } else if (mimeType?.startsWith("video") == true) {
            "video"
        } else {
            "unknown" // Fallback if it's neither image nor video
        }
    }

    private fun showLoader(show: Boolean) {
        if (show) activity?.showSimpleProgressDialog() else removeSimpleProgressDialog()
//        binding.editBtn.visibility = if (show) View.GONE else View.VISIBLE
    }
}