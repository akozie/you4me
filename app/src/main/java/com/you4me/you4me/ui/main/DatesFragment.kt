package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.android.billingclient.api.*
import com.bumptech.glide.Glide
import com.you4me.you4me.databinding.FragmentDatesBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.base.BaseFragment
import com.you4me.you4me.utils.Utils.getCategoryFromString
import com.you4me.you4me.utils.safeNavigate
import com.you4me.you4me.utils.safeNavigateUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatesFragment : BaseFragment<MainViewModel, FragmentDatesBinding, MainRepository>("RECEIVE_DATE_INTEREST") {
    private var dateInterests = FetchDateInterest()
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private lateinit var user: User
    private lateinit var date: FetchDateInterestItem
    private var userId: String? = null

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentDatesBinding {
        return FragmentDatesBinding.inflate(layoutInflater)
    }

    override fun getRepository() =
        MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve arguments
        arguments?.let {
            userId = it.getString("user_id")
        }

        binding.loader.show()
        setupObservers()
        setupView()

        // Set up the Toolbar using View Binding
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        // Enable the up button (back arrow)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            // Set the title
            title = "Received Interests"
        }
        binding.toolbar.setNavigationOnClickListener(View.OnClickListener
        { findNavController().safeNavigateUp() })
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun observeImagesAndVideos(userId: String) {
        viewModel.getImagesAndVideos(userId)
        viewModel.getImagesAndVideos.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = it.value
                    try {
                        // Your potentially crashing code (e.g., loading images, videos, etc.)
                        if (isAdded() && getActivity() != null) {
                            // Perform operations safely
                            loadImagesAndVideosInBackground(listOfImagesAndVideos)
//                            Log.d("JUST_CHECKING", "$listOfImagesAndVideos")
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
        val imageViews = listOf(binding.frame1, binding.frame2, binding.frame3, binding.frame4)

        var isProfilePictureSet = false

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                listOfImagesAndVideos.take(imageViews.size).asReversed()
                    .forEachIndexed { index, fileData ->
                        val frame = imageViews[index]

                        withContext(Dispatchers.Main) {
                            if (!isAdded) return@withContext

                            if (fileData.fileURL.isEmpty()) {
                                frame.visibility = View.GONE // Hide empty frames
                                return@withContext
                            } else {
                                frame.visibility = View.VISIBLE // Show frames with content
                            }

                            frame.removeAllViews() // Clear previous views
                            frame.isClickable = true
                            frame.isFocusable = true

                            val secureUrl = fileData.fileURL.replace("http://", "https://")

                            if (getCategoryFromString(fileData.fileURL) == "image") {
                                val imageView =
                                    ImageView(requireActivity()).apply {
                                        layoutParams =
                                            FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                            )
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                    }

                                Glide.with(requireActivity())
                                    .load(secureUrl)
                                    .into(imageView)

                                frame.addView(imageView)
//                                Log.d(
//                                    "IMAGES_RESSS",
//                                    "Added image: $secureUrl to frame: ${frame.id}"
//                                )

                                if (!isProfilePictureSet) {
                                    isProfilePictureSet = true
                                    Glide.with(requireActivity())
                                        .load(secureUrl)
                                        .into(binding.imageView)
                                    binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                            } else if (getCategoryFromString(fileData.fileURL) == "video") {
                                val thumbnailView =
                                    ImageView(requireContext()).apply {
                                        layoutParams =
                                            FrameLayout.LayoutParams(
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                                FrameLayout.LayoutParams.MATCH_PARENT,
                                            )
                                        scaleType = ImageView.ScaleType.CENTER_CROP
                                    }

                                val bitmap = generateVideoThumbnail(secureUrl)

                                if (bitmap != null) {
                                    thumbnailView.setImageBitmap(bitmap)
                                    frame.addView(thumbnailView)
//                                    Log.d("IMAGES_RESSS", "Added video thumbnail for: $secureUrl")
                                } else {
                                    Log.e(
                                        "IMAGES_RESSS",
                                        "Failed to generate thumbnail for: $secureUrl"
                                    )
                                }
                            }

                            frame.setOnClickListener {
                                if (fileData.fileURL.isEmpty()) return@setOnClickListener
                                openDetailScreen(
                                    secureUrl,
                                    getCategoryFromString(fileData.fileURL),
                                    fileData.videoId
                                )
                            }
                        }
                    }
                // Hide remaining frames that didn't get used
                withContext(Dispatchers.Main) {
                    for (i in listOfImagesAndVideos.size until imageViews.size) {
                        imageViews[i].visibility = View.GONE
                    }
                }
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
            )
        val action =
            DatesFragmentDirections.actionDatesFragmentToImageAndVideoDetailsFragment(
                imagesVideosResponseItem
            )
        findNavController().safeNavigate(action)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        binding.acceptBtn.setOnClickListener {
            showLoading(true)
            viewModel.updateDateInterest(
                date.interestID,
                date.dateID, "PENDING_TIME_APPROVAL"
            )
        }

        binding.rejectBtn.setOnClickListener {
            showLoading(true)
            viewModel.rejectDateInterest(
                date.interestID,
                date.dateID,
                "REJECTED",
            )
        }
    }

    private fun setupObservers() {
        viewModel.fetchDateInterests.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) {
                        showEmpty()
                    } else {
                        dateInterests = it.value
                        setScreen()
                    }
                }

                is Resource.Failure -> {
                    binding.constraintLayout2.visibility = View.VISIBLE
                    binding.mainLyt.visibility = View.GONE
                    binding.mainLytBtn.visibility = View.GONE
                }
            }
        }

        viewModel.rejectDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    findNavController().safeNavigateUp()
                }

                is Resource.Failure -> {
                    showAlertDialog(
                        requireContext(), it.message
                            ?: it.errorBody ?: "", "OK"
                    ) {}
                }
            }
        }
        viewModel.updateDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    findNavController().safeNavigateUp()
                }

                is Resource.Failure -> {
                    showAlertDialog(
                        requireContext(), it.message ?: it.errorBody ?: "",
                        "OK"
                    ) {}
                }
            }
        }
    }

    private fun setScreen() {
        showLoading(false)

        date = dateInterests.firstOrNull { it.userID == userId } ?: run {
            // Handle the case where no matching dateInterest is found.
            showToast("No matching date interest found")
            findNavController().safeNavigateUp()
            return
        }

        observeImagesAndVideos(date.userID)

        val mediaItem = MediaItem.fromUri(date.videoURL.replace("http:",
            "https:"))
        player?.setMediaItems(listOf(mediaItem), mediaItemIndex,
            playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.prepare()

        binding.userName.text = "${date.name}, ${date.age}"
        binding.nameGallery.text = "${date.name}'s Gallery"
        binding.location.text = "${date.venue} Gallery"
        binding.nameGallery.text = "${date.name}'s Gallery"

//    binding.bio.visibility = if (date.bio.isNotEmpty()) View.GONE else View.VISIBLE
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            mediaItemIndex = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer() {
//        player =
//            ExoPlayer.Builder(ctx).build().also {
//                binding.userVideo.player = it
//                if (currentIdx != -1) {
//                    val mediaItem =
//                        MediaItem.fromUri(dateInterests[currentIdx].videoURL.replace("http:", "https:"))
//                    it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
//                    it.playWhenReady = playWhenReady
//                    it.prepare()
//                }
//            }
    }

    private fun showEmpty() {
        findNavController().safeNavigateUp()

        binding.mainLyt.visibility = View.GONE
        binding.mainLytBtn.visibility = View.GONE

        // Get the current layout parameters
        val layoutParams = binding.cardView.layoutParams as? ViewGroup.MarginLayoutParams

        // Check if the cast was successful
        layoutParams?.let {
            it.bottomMargin = 0
            binding.cardView.layoutParams = it
        }

        binding.constraintLayout2.visibility = View.VISIBLE
//        binding.emptyLyt.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.mainLytBtn.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }
}


