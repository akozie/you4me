package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils.getCategoryFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FindDateFragment : BaseFragment<MainViewModel, FragmentFindDateBinding, MainRepository>("FIND_DATE") {
    private var dates = ArrayList<FetchDatesResponseItem>()
    private var currentIdx = -1

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private lateinit var date: FetchDatesResponseItem
    private var paymentModes: ArrayList<ValueLabelResponse>? = null
    private lateinit var user: User

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
         user = gson.fromJson(userProfile, User::class.java)
        setupView()
        setupObservers()
        showBottomSheetDialog()
        mixpanel?.track("Android_Find_Date_Viewed")


        //Report Abuse dialog
        binding.reportAbuse.setOnClickListener {
            showLoading(true)
            showReportAbuseDialog()
        }
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
                        }
                    } catch (e: Exception) {
                    }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

    private fun setDefaultImage(frame: FrameLayout) {
        val defaultImageView = createImageView()
        Glide.with(requireActivity())
            .load(R.drawable.find_date_bg) // Replace with your actual default image resource
            .into(defaultImageView)

        frame.addView(defaultImageView)
        frame.visibility = View.VISIBLE
    }

    private fun createImageView(): ImageView {
        return ImageView(requireActivity()).apply {
            layoutParams =
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )
            scaleType = ImageView.ScaleType.CENTER_CROP
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

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val validMedia = listOfImagesAndVideos.filter { it.fileURL.isNotEmpty() } // Remove empty URLs

                validMedia.take(imageViews.size).asReversed().forEachIndexed { index, fileData ->
                    val frame = imageViews[index]
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

                        withContext(Dispatchers.Main) {
                            if (isAdded) {
                                Glide.with(requireActivity())
                                    .load(secureUrl)
                                    .into(imageView)
                                frame.addView(imageView)

                                if (!isProfilePictureSet) {
                                    isProfilePictureSet = true
                                    Glide.with(requireActivity())
                                        .load(fileData.fileURL)
                                        .override(
                                            com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
                                            com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
                                        )
                                        .into(binding.imageView)
                                    binding.imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                                }
                            }
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

                        withContext(Dispatchers.Main) {
                            if (isAdded && bitmap != null) {
                                thumbnailView.setImageBitmap(bitmap)
                                frame.addView(thumbnailView)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (isAdded) {
                            frame.setOnClickListener {
                                if (fileData.fileURL.isEmpty()) return@setOnClickListener
                                openDetailScreen(secureUrl, getCategoryFromString(fileData.fileURL), fileData.videoId)
                            }
                        }
                    }
                }

                // Remove unused frames on the Main thread, but keep the first frame visible if the list is empty
                withContext(Dispatchers.Main) {
                    if (validMedia.isEmpty()) {
                        imageViews.forEachIndexed { index, frame ->
                            if (index == 0) return@forEachIndexed // Keep the first frame visible
                            frame.removeAllViews()
                            frame.visibility = View.GONE
                        }
                    } else {
                        imageViews.drop(validMedia.size).forEach { frame ->
                            frame.removeAllViews()
                            frame.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun showReportAbuseDialog() {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_abuse, null)

        // Initialize UI elements
        val etFeedback = dialogView.findViewById<EditText>(R.id.et_feedback)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val policy = dialogView.findViewById<TextView>(R.id.see_policy_link)

        // Create AlertDialog
        val dialog =
            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

        // Handle submit button click
        btnSubmit.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            val obj =
                JsonObject().apply {
                    addProperty("date_id", date.dateId)
                    addProperty("user_id", date.userId)
                    addProperty("thumbs_up", false)
                    addProperty("comment", feedback)
                }
            viewModel.updateReview(obj)
            viewModel.updateReviewResponse.observe(viewLifecycleOwner) {
                showLoading(false)
                when (it) {
                    is Resource.Success -> {
                        mixpanel?.track("Android_Find_Date_Report_Date_Button_Clicked")
                        val d = dates[currentIdx]
                        viewModel.addSwipe(
                            d.dateId,
                            d.userId,
                            false,
                            user.userId,
                            )
                        viewModel.addSwipe.observe(viewLifecycleOwner) {
                            showLoading(false)
                            when (it) {
                                is Resource.Success -> {
                                    showToast("Report Submitted")
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

                    is Resource.Failure -> {
                    }
                }
            }

            dialog.dismiss() // Close the dialog
        }

        btnCancel.setOnClickListener {
            showLoading(false)
            dialog.dismiss()
        }
        policy.setOnClickListener {
            policy.text =
                HtmlCompat.fromHtml(
                    getString(R.string.we_frown_against_child_abuse_see_policy_here_play_your_part_in_reporting_a_suspected_child_abuse),
                    HtmlCompat.FROM_HTML_MODE_LEGACY,
                )
            policy.movementMethod = LinkMovementMethod.getInstance() // Makes the link clickable
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.you4me.social/child-abuse-policy"))
            it.context.startActivity(intent)
        }
        // Show the dialog
        dialog.show()
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
            if (currentIdx < 0 || currentIdx >= dates.size) {
                return@setOnClickListener // Prevents out-of-bounds access
            }
            val d = dates[currentIdx]
            showLoading(true)
            viewModel.addDateInterest(
                d.dateId,
                d.date,
                d.time,
                user.userId,
                d.userId,
            )
            mixpanel?.track("Android_Liked_Find_Date_Button_Pressed")
        }
        binding.rejectBtn.setOnClickListener {
            if (currentIdx < 0 || currentIdx >= dates.size) {
                return@setOnClickListener // Prevents out-of-bounds access
            }
            showLoading(true)
            val d = dates[currentIdx]
            viewModel.addSwipe(
                d.dateId,
                d.userId,
                false,
                user.userId,
                )
            mixpanel?.track("Android_Disliked_Find_Date_Button_Pressed")
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
                        if (currentIdx < 0 || currentIdx >= dates.size) {
                            // do nothing
                        } else {
                            showLoading(true)
                            val d = dates[currentIdx]
                            viewModel.addSwipe(
                                d.dateId,
                                d.userId,
                                false,
                                user.userId,
                                )
                        }
                    } else {
                        startSecondTiltAnimation(true)
                        if (currentIdx < 0 || currentIdx >= dates.size) {
                            // do nothing
                        } else {
                            showLoading(true)
                            val d = dates[currentIdx]
                            viewModel.addDateInterest(
                                d.dateId,
                                d.date,
                                d.time,
                                user.userId,
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

        if (user.status.toLowerCase() == "incomplete") {
            binding.completeProfileLayout.visibility = View.VISIBLE
            binding.constraintLayout2.visibility = View.GONE
            return
        } else {
            binding.completeProfileLayout.visibility = View.GONE
            //  Log.d("FUNNY_GIRL", user.userId)
            viewModel.fetchDates(user.userId)
        }

        viewModel.addDateInterest.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val d = dates[currentIdx]
                    viewModel.addSwipe(
                        d.dateId,
                        d.userId,
                        true,
                        user.userId,
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
        val sharedPreferences = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val hasDialogBeenShown = sharedPreferences.getBoolean("bottom_sheet_shown", false)

        if (!hasDialogBeenShown) {
            // Create the BottomSheetDialog
            val bottomSheetDialog = BottomSheetDialog(requireContext())

            // Inflate the layout for the dialog
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_layout, null)

            // Set up click listeners for actions inside the BottomSheetDialog
            view.findViewById<TextView>(R.id.okButton).setOnClickListener {
                // Perform some action
                bottomSheetDialog.dismiss()
            }

            // Dismiss dialog when clicking outside
            bottomSheetDialog.setOnDismissListener {
                // Update the flag in SharedPreferences
                with(sharedPreferences.edit()) {
                    putBoolean("bottom_sheet_shown", true)
                    apply()
                }
            }

            // Set the content view and show the dialog
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}
