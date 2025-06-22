package com.you4me.you4me.ui.profile.bottomsheet

import android.app.Activity
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.core.AppDatabase
import com.you4me.you4me.core.DbRepository
import com.you4me.you4me.databinding.FragmentCountryBottomSheetBinding
import com.you4me.you4me.databinding.FragmentEditProfilePictureBottomSheetBinding
import com.you4me.you4me.databinding.FragmentProfileBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.RegisterProfilePhotoBody
import com.you4me.you4me.models.RegisterVideoUploadBody
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.RemoteDataSource
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.repository.ProfileRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.ui.profile.ProfileFragment
import com.you4me.you4me.ui.profile.ProfileViewModel
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.showAlertDialog
import com.you4me.you4me.utils.removeSimpleProgressDialog
import com.you4me.you4me.utils.showSimpleProgressDialog
import java.io.File
import java.util.*


class EditProfilePictureBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentEditProfilePictureBottomSheetBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var repository: ProfileRepository
    private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var recordVideoLauncher: ActivityResultLauncher<Intent>
    private var videoUri: Uri? = null
    private lateinit var videoId: String
    private lateinit var user: User
    private lateinit var photoUri: Uri
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

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


        // Register in onCreate or onViewCreated
        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Handle the captured image (photoUri contains the image URI)
//                binding.image1.setImageURI(photoUri) // Example
                Log.d("URI_PHOTO", "$photoUri")
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
            viewModel.cameraUpload()
        }
        binding.chooseFromLibrary.setOnClickListener {
            showLoader(true)
            viewModel.validateVideoUpload()
        }
        binding.cancelIcon.setOnClickListener {
            dismiss()
        }
        setUpView()
        addObservers()

    }

    private fun setUpView(){
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    videoUri = it.data?.data ?: return@registerForActivityResult
                    if (validateMedia(videoUri!!)) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)

                        viewModel.registerProfilePhotoUpload(
                            RegisterProfilePhotoBody(
                                "$videoUri",
                                user.userId,
                                videoId,
                                getCategoryFromUri(requireContext(), videoUri!!),
                                true,
                            ),
                        )
//                        Log.d("YEPAAA", "$videoUri")
                    } else {
//                        showDialog("Video duration must not be longer than 30 seconds")
                    }
                }
            }


        recordVideoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Handle the recorded video URI (e.g., upload it to your server or save it locally)
                    videoUri = result.data?.data ?: return@registerForActivityResult
                    if (validateMedia(videoUri!!)) {
                        videoId = UUID.randomUUID().toString()
                        showLoader(true)

                        viewModel.registerProfilePhotoUpload(
                            RegisterProfilePhotoBody(
                                "$videoUri",
                                user.userId,
                                videoId,
                                getCategoryFromUri(requireContext(), videoUri!!),
                                true
                            ),
                        )
                    } else {
//                        showAlertDialog(requireContext(), "Video duration must not be longer than 30 seconds")
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
        val photoFile = File.createTempFile("IMG_", ".jpg", requireContext().cacheDir)
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
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
//                    binding.videoBannerLayout.isVisible = true
//                    binding.profileLayout.isVisible = false
//                    showVideoRegulationsDialog()
                    openCamera()
                }

                is Resource.Failure -> {
                    showLoader(false)
                    if (it.errorCode == 400) {
                        showToast("You already uploaded a video")
                    } else {
                        showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK", "", {}) {}
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
                        showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK", "", {}) {}
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

        viewModel.uploadError.observe(viewLifecycleOwner) {errorMessage ->
            errorMessage?.let {
                showToast(it)
            }
        }


        viewModel.updateVideoUrlResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    mixpanel?.track("Android_Profile_Uploaded_Media")
                    showToast("Uploaded Successfully")
//                    videoViewBinding.videoView.setVideoURI(videoUri)
//                    initializePlayer()
                    showLoader(true)
                    binding.root.isVisible = true
//                    binding.profileLayout.isVisible = true
                }

                is Resource.Failure -> {}
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