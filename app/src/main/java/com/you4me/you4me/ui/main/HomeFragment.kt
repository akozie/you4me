package com.you4me.you4me.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.*
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.*
import com.you4me.you4me.models.interests.Interest
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.SharedPrefHelper.Companion.PROFILE_IMAGE
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeFragment :
    BaseFragment<MainViewModel, FragmentHomeBinding, MainRepository>("HOME"){
    private lateinit var user: User

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity?.finishAffinity()
                }
            },
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        viewModel.getUserDetails(user.userId)
        addObservers()
        setUpViewPager()
        getImages()
        getTimeOfDay()
//        viewModel.getSubscriptionStatusForHome(user.userId)
        binding.notificationIcon.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment) }
        mixpanel?.track("Android_Home_Viewed")


        // Check if we need to navigate to ProfileFragment
        val navigateToProfile = activity?.intent?.getStringExtra("navigate_to") == "profile"

        if (navigateToProfile && !viewModel.hasNavigatedToProfile) {
            viewModel.hasNavigatedToProfile = true // Mark as navigated

            // Clear the intent extra to prevent re-triggering
            activity?.intent?.removeExtra("navigate_to")

            // Navigate to ProfileFragment
            findNavController().navigate(R.id.profileFragment)
        }

        binding.chat.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }

        binding.viewAllDateProposalsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_allDateProposalsFragment)
        }

        binding.createADateLayout.setOnClickListener {
            findNavController().navigate(R.id.goOnDateFragment)
        }

        binding.findDatesLyt.setOnClickListener {
            findNavController().navigate(R.id.findDateFragment)
        }
    }

    private fun getTimeOfDay(){
        val currentTime = Calendar.getInstance()

        val greeting = when (currentTime.get(Calendar.HOUR_OF_DAY)) {
            in 1..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }

        binding.timeOfDay.text = greeting
    }
    private fun getImages() {
        viewModel.getImagesAndVideos(user.userId)
        viewModel.getImagesAndVideos.observe(viewLifecycleOwner) { images ->
            when (images) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = images.value
                    try {
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
        val profileImageView = binding.profileImage

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

    private fun addObservers() {
        viewModel.existingUser.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    viewModel._user.value = it.value
//                    viewModel.getUpcomingDates(user.userId)
                    viewModel.receivedDateInterests(user.userId)
//                    viewModel.getInviteeDatesRequiringApproval(user.userId)
//                    viewModel.getDateInterestsRequiringApproval()
                    viewModel.getNotifications()
//                    viewModel.fetchCompletedDates()
                    setUpPremiumBanner(user)
                }

                is Resource.Failure -> {
                    Log.d("TIREDDD", "${it.message}")
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }




        viewModel.receivedInterestDateInterests.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupInviteeDates(it.value.interests)
                }

                is Resource.Failure -> {
                    Log.d("TIREDDD1", "${it.message}")
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }


//        viewModel.inviteeDatesRequiringApproval.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
////                    setupInviteeDates(it.value)
//                    val list =  InviteeDatesRequiringApproval().apply {
//                        add(
//                            InviteeDatesRequiringApprovalItem(
//                                "CHAT",
//                                "2025/06/09",
//                                "12wqasde",
//                                "12wqasde111",
//                                "Emmanuel",
//                                "Lekki",
//                                "2025/06/09",
//                                "13:20",
//                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
//                            )
//                        )
//                    }
//                    setupInviteeDates(list)
//                }
//
//                is Resource.Failure -> {
//                    showToast(it.message ?: it.errorBody ?: "")
//                }
//            }
//        }


//        viewModel.dateInterestsRequiringApproval.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    setupDateInterests(it.value)
//                }
//
//                is Resource.Failure -> {}
//            }
//        }

        viewModel.rejectDateInterest.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast("Date status updated")
                    // switch ui
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.updateDateInterest.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast("Date status updated")
                    // switch ui
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.getNotificationsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val unseenCount =
                        it.value.mapNotNull { n ->
                            if (n.seen.lowercase() == "false") n else null
                        }.size
                    if (it.value.isEmpty() || unseenCount < 1) {
                        binding.unreadNotificationsDot.visibility = View.GONE
                    } else {
                        binding.unreadNotificationsDot.text = unseenCount.toString()
                        binding.unreadNotificationsDot.visibility = View.VISIBLE
                    }
                }
                is Resource.Failure -> {
                }
            }
        }

//        viewModel.getSubscriptionStatusForHome.observe(viewLifecycleOwner) {
//            when (it) {
//                is Resource.Success -> {
//                    sharedPrefHelper.saveBoolean(IS_FREE_PLAN, it.value.isFreeTrial)
//                }
//
//                is Resource.Failure -> {
//                    //
//                }
//            }
//        }
    }

    // Override onActivityResult to handle the result of the calendar activity
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EVENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    private fun setUpPremiumBanner(user: User) {
        if (user.status.toLowerCase().contains("complete")) {
            binding.alreadySubscribed.isVisible = true
            binding.notYetSubscribed.isVisible = false
        } else {
            binding.alreadySubscribed.isVisible = false
            binding.notYetSubscribed.isVisible = true
        }

        if (user.name.isNotEmpty()) {
            binding.userName.isVisible = true
            binding.userName.text = user.name
        } else {
            binding.userName.isVisible = false
        }
    }

//    private fun setupDateInterests(dates: DateInterestsRequiringApproval) {
//        if (dates.isEmpty()) {
//            binding.datesRequiringAppLyt.visibility = View.GONE
//            binding.dateInterestRecycler.visibility = View.GONE
//        } else {
//            binding.datesRequiringAppLyt.visibility = View.VISIBLE
//            binding.dateInterestRecycler.visibility = View.VISIBLE
//            val adapter =
//                mixpanel?.mixpanel?.let {
//                    DateInterestsRequiringApprovalRecyclerAdapter(
//                        dates,
//                        viewModel,
//                        it,
//                    )
//                }
//            binding.dateInterestRecycler.adapter = adapter
//        }
//    }


    private fun setupInviteeDates(dates: List<Interest>) {
        if (dates.isEmpty()) {
            binding.approvedDatesLyt.visibility = View.GONE
            binding.approvedDatesLytTxt.visibility = View.GONE
            binding.approvedDatesLytView.visibility = View.GONE
            binding.noDatesLyt.visibility = View.VISIBLE
            binding.boostLayout.visibility = View.GONE
        } else {
            binding.approvedDatesLyt.visibility = View.VISIBLE
            binding.approvedDatesLytTxt.visibility = View.VISIBLE
            binding.approvedDatesLytView.visibility = View.VISIBLE
            binding.noDatesLyt.visibility = View.GONE
            binding.boostLayout.visibility = View.VISIBLE
            val adapter =
                mixpanel?.mixpanel?.let { InviteeDateForApprovalRecyclerAdapter(dates, viewModel, ctx, it) }
            binding.inviteeDatesRecycler.adapter = adapter
        }
    }

//    private fun setupInviteeDates(dates: InviteeDatesRequiringApproval) {
//        if (!dates.isEmpty()) {
//            binding.approvedDatesLyt.visibility = View.GONE
//            binding.approvedDatesLytTxt.visibility = View.GONE
//            binding.approvedDatesLytView.visibility = View.GONE
//            binding.noDatesLyt.visibility = View.VISIBLE
//            binding.boostLayout.visibility = View.GONE
//        } else {
//            binding.approvedDatesLyt.visibility = View.VISIBLE
//            binding.approvedDatesLytTxt.visibility = View.VISIBLE
//            binding.approvedDatesLytView.visibility = View.VISIBLE
//            binding.noDatesLyt.visibility = View.GONE
//            binding.boostLayout.visibility = View.VISIBLE
//            val adapter =
//                mixpanel?.mixpanel?.let { InviteeDateForApprovalRecyclerAdapter(dates, viewModel, ctx, it) }
//            binding.inviteeDatesRecycler.adapter = adapter
//        }
//    }

    private fun setUpViewPager() {
        val adapter = HomePagerAdapter(this, 2) // ✅ Pass `this` (fragment)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = true // ✅ Ensure swiping is enabled

        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.text =
                when (position) {
                    0 -> getString(R.string.upcoming_dates)
                    1 -> getString(R.string.completed_dates)
                    else -> getString(R.string.upcoming_dates)
                }
        }.attach()
    }


    override fun onDestroy() {
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
        super.onDestroy()
    }
}
