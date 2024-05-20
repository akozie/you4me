package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper


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
        container: ViewGroup?
    ): FragmentFindDateBinding {
        return FragmentFindDateBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        //  user = gson.fromJson(userProfile, User::class.java)
        setupView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
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
                false
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
                            //do nothing
                        } else {
                            showLoading(true)
                            val d = dates[currentIdx]
                            viewModel.addSwipe(
                                d.dateId,
                                d.userId,
                                false
                            )
                        }
                    } else {
                        startSecondTiltAnimation(true)
                        if (currentIdx < 0) {
                            //do nothing
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
                    if (it.value.isEmpty()) showEmpty()
                    else {
                        dates = it.value
                        val dummyData = FetchDatesResponse().apply {
                            add(
                                FetchDatesResponseItem(
                                    "19",
                                    "2024-03-05",
                                    "1",
                                    "1",
                                    "Conference Room",
                                    "2024-04-05",
                                    "09:00",
                                    "1",
                                    "19:00",
                                    "12345",
                                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                )
                            )
                            add(
                                FetchDatesResponseItem(
                                    "19",
                                    "2024-03-05",
                                    "1",
                                    "1",
                                    "Conference Room",
                                    "2024-04-05",
                                    "09:00",
                                    "1",
                                    "19:00",
                                    "12345",
                                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                )
                            )
                            add(
                                FetchDatesResponseItem(
                                    "19",
                                    "2024-03-05",
                                    "1",
                                    "1",
                                    "Conference Room",
                                    "2024-04-05",
                                    "09:00",
                                    "1",
                                    "19:00",
                                    "12345",
                                    "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                                )
                            )
                        }
//                        dates = dummyData
                        setScreen()
                        binding.mainLyt.visibility = View.VISIBLE
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
                        true
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

        binding.userName.text = "${date.name}, ${date.age}"
        binding.location.text = date.place
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
        player = ExoPlayer.Builder(ctx).build().also {
            binding.userVideo.player = it
            if (currentIdx != -1) {
                val mediaItem =
                    MediaItem.fromUri(dates[currentIdx].videoURL.replace("http:", "https:"))
                it.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
                it.playWhenReady = playWhenReady
                it.prepare()
            }
        }
    }

    private fun showEmpty() {
        binding.lottieAnimationView.setAnimation("dating.json")
        binding.lottieAnimationView.playAnimation()
        binding.mainLyt.visibility = View.GONE
        binding.constraintLayout2.visibility = View.VISIBLE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        //binding.constraintLayout2.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

}