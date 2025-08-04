package com.you4me.you4me.ui.main.finddates

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.finddates.ImagePagerAdapter
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.databinding.FragmentNearbyBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.FetchDatesResponseItem
import com.you4me.you4me.models.ImagesVideosResponseItem
import com.you4me.you4me.models.useroptions.PaymentModes
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.utils.SharedPrefHelper


class NearbyFragment :
    BaseFragment<MainViewModel, FragmentNearbyBinding, MainRepository>("FIND_DATE") {

    private var dates = ArrayList<FetchDatesResponseItem>()
    private var currentIdx = -1

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var mediaItemIndex = 0
    private var playbackPosition = 0L
    private lateinit var date: FetchDatesResponseItem
    private var paymentModes: ArrayList<PaymentModes>? = null
    private lateinit var user: User

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentNearbyBinding {
        return FragmentNearbyBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        setupView()
        setupObservers()
        mixpanel?.track("Android_Find_Date_Viewed")

        binding.filter.setOnClickListener {
            findNavController().navigate(R.id.action_findDateFragment_to_filterBottomSheetFragment)
        }
        binding.adjustFilter.setOnClickListener {
            findNavController().navigate(R.id.action_findDateFragment_to_filterBottomSheetFragment)
        }
    }

    private fun setupView() {
//        mediaControls = MediaController(ctx)
//        mediaControls.setAnchorView(binding.userVideo)
//        mediaControls.setMediaPlayer(binding.userVideo)
//        binding.userVideo.setMediaController(mediaControls)

        binding.acceptBtn.setOnClickListener {
            findNavController().navigate(R.id.action_findDateFragment_to_dateMatchFragment)
//            if (currentIdx < 0 || currentIdx >= dates.size) {
//                return@setOnClickListener // Prevents out-of-bounds access
//            }
//            val d = dates[currentIdx]
//            showLoading(true)
//            viewModel.addDateInterest(
//                d.dateId,
//                d.date,
//                d.time,
//                user.userId,
//                d.userId,
//            )
//            mixpanel?.track("Android_Liked_Find_Date_Button_Pressed")
        }
        binding.rejectBtn.setOnClickListener {
            findNavController().navigate(R.id.action_findDateFragment_to_notInterestedFragment)
//            if (currentIdx < 0 || currentIdx >= dates.size) {
//                return@setOnClickListener // Prevents out-of-bounds access
//            }
//            showLoading(true)
//            val d = dates[currentIdx]
//            viewModel.addSwipe(
//                d.dateId,
//                d.userId,
//                false,
//                user.userId,
//            )
//            mixpanel?.track("Android_Disliked_Find_Date_Button_Pressed")
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
//        viewModel.fetchPaymentModes()
//        viewModel.paymentModes.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    paymentModes = it.value
//                }
//
//                is Resource.Failure -> {
//                    showToast(it.message ?: it.errorBody ?: "")
//                }
//            }
//        }

        viewModel.getUserOptions()
        viewModel.getUserOptionsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    paymentModes = it.value.paymentModes
                }

                is Resource.Failure -> {
                    showAlertDialog(requireContext(), it.message ?: it.errorBody ?: "", "OK") {}
                }
            }
        }
        if (user.status.toLowerCase() != "incomplete") {
            binding.completeProfileLayout.visibility = View.VISIBLE
            binding.constraintLayout2.visibility = View.GONE
            Log.d("FUNNY_GIRL===", user.userId)
//            return
        } else {
            showLoading(true)
            binding.completeProfileLayout.visibility = View.GONE
            viewModel.fetchDates(user.userId)
            viewModel.fetchDates.observe(viewLifecycleOwner) {
                showLoading(false)
                when (it) {
                    is Resource.Success -> {
//                        Log.d("FUNNY_GIRL", user.userId)
                        if (it.value.dates.isEmpty()) {
                            showEmpty()
//                            showToast("EMPTY")
                        } else {
                            dates = it.value.dates
//                            showToast("NOT EMPTY")
                            val list =  arrayListOf(
                                FetchDatesResponseItem(
                                    "30",
                                    "2025/06/09",
                                    "2025/06/09",
                                    "12wqasde111",
                                    "Emmanuel",
                                    "ME",
                                    "Lekki Phase 1",
                                    "Football lover",
                                    "2025/06/09",
                                    "2025/06/09",
                                    "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8",
                                    "",
                                    "",
                                    ""
                                ),
                                FetchDatesResponseItem(
                                    "30",
                                    "2025/06/09",
                                    "2025/06/09",
                                    "12wqasde111",
                                    "Emmanuel",
                                    "ME",
                                    "Lekki Phase 1",
                                    "Football lover",
                                    "2025/06/09",
                                    "2025/06/09",
                                    "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8",
                                    "",
                                    "",
                                    ""
                                )
                            )
//                            dates = list
                            setScreen()
                            binding.mainLyt.visibility = View.VISIBLE
                            binding.mainLytBtn.visibility = View.VISIBLE
                        }
                    }

                    is Resource.Failure -> {
                        Log.d("FUNNY_GIRL_NOT", user.userId)
                        showToast(it.message ?: it.errorBody ?: "")
                    }
                }
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

//        binding.userName.text = "${date.name.trim()}, ${date.age}"
//        binding.nameGallery.text = "${date.name.trim()}'s Gallery"
//        binding.location.text = date.place
//        binding.bio.text = date.bio
//        binding.payment.text =
//            paymentModes?.firstOrNull { it.value == date.payment }?.label ?: date.payment

        val mediaItem = MediaItem.fromUri(date.videoURL.replace("http:", "https:"))
        player?.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
        player?.playWhenReady = playWhenReady
        player?.prepare()
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
//                            loadImagesAndVideosInBackground(listOfImagesAndVideos)
                            loadImages(listOfImagesAndVideos)
                        }
                    } catch (e: Exception) {
                    }
                }

                is Resource.Failure -> {
                }
            }
        }
    }

    private fun loadImages(imageUrls: List<ImagesVideosResponseItem>) {
//        val imageUrls = listOf(
//            R.drawable.image1,
//            R.drawable.image2,
//            R.drawable.image3
//        )

        val adapter = ImagePagerAdapter(imageUrls)
        binding.imageViewPager.adapter = adapter

        TabLayoutMediator(binding.tabIndicator, binding.imageViewPager) { _, _ -> }.attach()
    }

    private fun showEmpty() {
        binding.lottieAnimationView.setAnimation("dating.json")
        binding.lottieAnimationView.playAnimation()
        binding.mainLyt.visibility = View.GONE
        binding.mainLytBtn.visibility = View.GONE
        binding.constraintLayout2.visibility = View.VISIBLE
        binding.constraintLayout.visibility = View.VISIBLE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.mainLytBtn.visibility = if (loading) View.GONE else View.VISIBLE
        // binding.constraintLayout2.visibility = if (loading) View.GONE else View.VISIBLE
        binding.constraintLayout.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

}