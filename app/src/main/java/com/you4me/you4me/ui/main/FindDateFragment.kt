package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.generateVideoThumbnail
import com.you4me.you4me.utils.Utils.getCategoryFromString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FindDateFragment : BaseFragment<MainViewModel, FragmentFindDateBinding, MainRepository>() {
    private var dates = ArrayList<FetchDatesResponseItem>()
    private var currentIdx = -1

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private lateinit var date: FetchDatesResponseItem
    private var paymentModes: ArrayList<ValueLabelResponse>? = null

    //  private lateinit var user: User
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentFindDateBinding {
        return FragmentFindDateBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        //  user = gson.fromJson(userProfile, User::class.java)
        setupView()
        setupObservers()
        showBottomSheetDialog()
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
                        loadImagesAndVideosInBackground(listOfImagesAndVideos)
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
        val imageViews =
            listOf(
                binding.frame1,
                binding.frame2,
                binding.frame3,
                binding.frame4,
            ) // Predefined ImageViews
        var isProfilePictureSet = false // Flag to check if profile picture is already set

        CoroutineScope(Dispatchers.Main).launch {
            // Run the heavy task on the IO thread
            withContext(Dispatchers.Main) {
                listOfImagesAndVideos.take(imageViews.size).asReversed().forEachIndexed { index, fileData ->
                    val frame = imageViews[index]
                    frame.isClickable = true
                    frame.isFocusable = true

                    // Replace 'http' with 'https' for secure URLs
                    val secureUrl = fileData.fileURL.replace("http://", "https://")

                    if (getCategoryFromString(fileData.fileURL) == "image") {
                        // Load image into FrameLayout
                        val imageView = ImageView(requireActivity())
                        imageView.layoutParams =
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT,
                            )
                        imageView.scaleType = ImageView.ScaleType.FIT_XY

                        // Load image using Glide (this is still safe on the main thread since Glide handles threading internally)
                        Glide.with(requireActivity())
                            .load(secureUrl)
                            .into(imageView)

                        // Add to FrameLayout in the main thread
                        withContext(Dispatchers.Main) {
                            frame.addView(imageView)
                            if (!isProfilePictureSet) {
                                isProfilePictureSet = true // Mark profile picture as set
                                Glide.with(requireActivity())
                                    .load(fileData.fileURL) // URL of the first image
                                    .circleCrop()
                                    .into(binding.imageView)
                                binding.imageView.scaleType = ImageView.ScaleType.FIT_XY
                            }
                        }
                    } else if (getCategoryFromString(fileData.fileURL) == "video") {
                        // Load video thumbnail into FrameLayout
                        val thumbnailView = ImageView(requireContext())
                        thumbnailView.layoutParams =
                            FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT,
                            )
                        thumbnailView.scaleType = ImageView.ScaleType.FIT_XY

                        // Generate thumbnail using MediaMetadataRetriever in the background
                        val bitmap = generateVideoThumbnail(secureUrl)

                        // Add thumbnail to FrameLayout in the main thread
                        withContext(Dispatchers.Main) {
                            if (bitmap != null) {
                                thumbnailView.setImageBitmap(bitmap)
                            }
                            frame.addView(thumbnailView)
                        }
                    }

                    // Add a click listener to the frame
                    withContext(Dispatchers.Main) {
                        frame.setOnClickListener {
                            if (fileData.fileURL.isEmpty()) {
//                                showLoading(true)
                                return@setOnClickListener
                            }
                            openDetailScreen(secureUrl, getCategoryFromString(fileData.fileURL), fileData.videoId)
                        }
                    }
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
            )
        val action = FindDateFragmentDirections.actionFindDateFragmentToImageAndVideoDetailsFragment(imagesVideosResponseItem)
        findNavController().navigate(action)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
//        mediaControls = MediaController(ctx)
//        mediaControls.setAnchorView(binding.userVideo)
//        mediaControls.setMediaPlayer(binding.userVideo)
//        binding.userVideo.setMediaController(mediaControls)

        binding.acceptBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            val d = dates[currentIdx]
            showLoading(true)
            viewModel.addDateInterest(
                d.dateId,
                d.date,
                d.time,
                d.userId,
            )
        }
        binding.rejectBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dates[currentIdx]
            viewModel.addSwipe(
                d.dateId,
                d.userId,
                false,
            )
        }

        binding.mainLyt.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Save the initial touch position
                    binding.mainLyt.setTag(R.id.tag_touch_start_x, event.x)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Calculate the swipe distance
                    val startX = binding.mainLyt.getTag(R.id.tag_touch_start_x) as Float
                    val endX = event.x
                    val swipeDistance = endX - startX

                    // Apply the tilt animation based on the swipe direction
                    if (swipeDistance > 0) {
                        startTiltAnimation(true)
                        if (currentIdx < 0) {
                            // do nothing
                        } else {
                            showLoading(true)
                            val d = dates[currentIdx]
                            viewModel.addSwipe(
                                d.dateId,
                                d.userId,
                                false,
                            )
                        }
                    } else {
                        startSecondTiltAnimation(true)
                        if (currentIdx < 0) {
                            // do nothing
                        } else {
                            showLoading(true)
                            val d = dates[currentIdx]
                            viewModel.addDateInterest(
                                d.dateId,
                                d.date,
                                d.time,
                                d.userId,
                            )
                        }
                    }
                    true
                }
                else -> false
            }
        }

//        binding.mainLyt.setOnTouchListener(object : OnSwipeTouchListener(ctx) {
//            override fun onSwipeLeft() {
//                view?.performClick()
//                super.onSwipeLeft()
//                if (currentIdx < 0) return
//                showLoading(true)
//                val d = dates[currentIdx]
//                viewModel.addSwipe(
//                    d.dateId,
//                    d.userId,
//                    false
//                )
//            }
//
//
//            override fun onSwipeRight() {
//                view?.performClick()
//                super.onSwipeRight()
//                if (currentIdx < 0) return
//                showLoading(true)
//                val d = dates[currentIdx]
//                viewModel.addDateInterest(
//                    d.date,
//                    d.time,
//                    d.userId,
//                )
//            }
//        })
    }

    private fun startTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
        binding.mainLyt.startAnimation(tiltAnimation)
    }

    private fun startSecondTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.second_tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
        binding.mainLyt.startAnimation(tiltAnimation)
    }

    private fun setupObservers() {
        viewModel.fetchPaymentModes()
        viewModel.paymentModes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    paymentModes = it.value
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.fetchDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) {
                        showEmpty()
                    } else {
                        dates = it.value
                        Log.d("FETCH_DATES", "$dates")
//                        observeImagesAndVideos(dates)
//                        val dummyData =
//                            FetchDatesResponse().apply {
//                                add(
//                                    FetchDatesResponseItem(
//                                        "19",
//                                        "2024-03-05",
//                                        "1",
//                                        "1",
//                                        "Conference Room",
//                                        "2024-04-05",
//                                        "09:00",
//                                        "1",
//                                        "19:00",
//                                        "12345",
//                                        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                                    ),
//                                )
//                                add(
//                                    FetchDatesResponseItem(
//                                        "19",
//                                        "2024-03-05",
//                                        "1",
//                                        "1",
//                                        "Conference Room",
//                                        "2024-04-05",
//                                        "09:00",
//                                        "1",
//                                        "19:00",
//                                        "12345",
//                                        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                                    ),
//                                )
//                                add(
//                                    FetchDatesResponseItem(
//                                        "19",
//                                        "2024-03-05",
//                                        "1",
//                                        "1",
//                                        "Conference Room",
//                                        "2024-04-05",
//                                        "09:00",
//                                        "1",
//                                        "19:00",
//                                        "12345",
//                                        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
//                                    ),
//                                )
//                            }
                        val dummyData =
                            FetchDatesResponse().apply {
                                add(
                                    FetchDatesResponseItem(
                                        age = "23",
                                        createdAt = "",
                                        date = "January30, 2025",
                                        dateId = "60be36ed-3cdb-4ea2-bd52-8d7f0eab0d05",
                                        name = "Emmanuel ",
                                        bio = "Emmanuel ",
                                        payment = "2",
                                        place = "Lekki Lekki, Lagos Lekki, Lagos",
                                        rawTime = "2025/01/30 18:45",
                                        time = "18:45",
                                        userId = "255e21fe-0407-40f3-8331-9a4bcda4ebae",
                                        videoURL = "http://res.cloudinary.com/mmuodev/video/upload/v1720882670/kd8opas6u6czkgivnsh4.mp4",
                                    ),
                                )
                                add(
                                    FetchDatesResponseItem(
                                        age = "26",
                                        createdAt = "",
                                        date = "January 11, 2025",
                                        dateId = "643c9444-b387-45a3-ade9-a0d10e066449",
                                        name = "New name",
                                        bio = "New name",
                                        payment = "2",
                                        place = "Lekki Lekki, Lagos Lekki, Lagos",
                                        rawTime = "2025/01/11 20:15",
                                        time = "20:15",
                                        userId = "9bbea5f7-f0c6-4c81-9855-795c73fd1338",
                                        videoURL = "",
                                    ),
                                )
                            }
//                        dates = dummyData
                        setScreen()
                        binding.mainLyt.visibility = View.VISIBLE
                        binding.mainLytBtn.visibility = View.VISIBLE
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) {
            // Log.d("OK_FUNNY_GIRL", user.userId)
            if (it.status == "incomplete") {
                binding.completeProfileLayout.visibility = View.VISIBLE
                binding.constraintLayout2.visibility = View.GONE
                return@observe
            } else {
                binding.completeProfileLayout.visibility = View.GONE
                //  Log.d("FUNNY_GIRL", user.userId)
                viewModel.fetchDates()
            }
        }
        viewModel.addDateInterest.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val d = dates[currentIdx]
                    viewModel.addSwipe(
                        d.dateId,
                        d.userId,
                        true,
                    )
                }

                is Resource.Failure -> {
                    showLoading(false)
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.addSwipe.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dates.lastIndex) {
                        setScreen()
                    } else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setScreen() {
        currentIdx++
        if (currentIdx < 0 || currentIdx >= dates.size) {
            // Don't use this index. This is out of bounds (borders, limits, whatever).
        } else {
            // Yes, you can safely use this index. The index is present in the array.
            date = dates[currentIdx]
        }
        observeImagesAndVideos(date.userId)

        binding.userName.text = "${date.name.trim()}, ${date.age}"
        binding.nameGallery.text = "${date.name.trim()}'s Gallery"
        binding.location.text = date.place
        binding.bio.text = date.bio
        binding.payment.text =
            paymentModes?.firstOrNull { it.value == date.payment }?.label ?: date.payment

        val mediaItem = MediaItem.fromUri(date.videoURL.replace("http:", "https:"))
        player?.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.prepare()
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
//                        MediaItem.fromUri(dates[currentIdx].videoURL.replace("http:", "https:"))
//                    it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
//                    it.playWhenReady = playWhenReady
//                    it.prepare()
//                }
//            }
    }

    private fun showEmpty() {
        binding.lottieAnimationView.setAnimation("dating.json")
        binding.lottieAnimationView.playAnimation()
        binding.mainLyt.visibility = View.GONE
        binding.mainLytBtn.visibility = View.GONE
        binding.constraintLayout2.visibility = View.VISIBLE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.mainLytBtn.visibility = if (loading) View.GONE else View.VISIBLE
        // binding.constraintLayout2.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showBottomSheetDialog() {
        // Create the BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the layout for the dialog
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null)

        // Set up click listeners for actions inside the BottomSheetDialog
        view.findViewById<TextView>(R.id.okButton).setOnClickListener {
            // Perform some action
            bottomSheetDialog.dismiss()
        }

        // Set the content view and show the dialog
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.show()
    }
}
