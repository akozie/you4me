package com.you4me.you4me.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.CompletedDatesRecyclerAdapter
import com.you4me.you4me.adapter.DateInterestsRequiringApprovalRecyclerAdapter
import com.you4me.you4me.adapter.InviteeDateForApprovalRecyclerAdapter
import com.you4me.you4me.adapter.UpcomingDatesRecyclerAdapter
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.SharedPrefHelper.Companion.IS_FREE_PLAN
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import java.util.*

class HomeFragment :
    BaseFragment<MainViewModel, FragmentHomeBinding, MainRepository>("HOME"),
    UpcomingDatesRecyclerAdapter.CalendarResultListener {
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
        Log.d("PROFILEID", userProfile)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
//         viewModel.getNewUser(requireContext())
        viewModel.getUserDetails(user.userId)
        addObservers()
        viewModel.getSubscriptionStatusForHome(user.userId)
        binding.notificationIcon.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment) }
        mixpanel?.track("Android_Home_Viewed")
    }

    private fun addObservers() {
        viewModel.existingUser.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    Log.d("OK_DONR", it.value.toString())
//                    viewModel._user.value = it.value
                    viewModel.getUpcomingDates()
                    viewModel.getInviteeDatesRequiringApproval(user.userId)
                    viewModel.getDateInterestsRequiringApproval()
                    viewModel.getNotifications()
                    viewModel.fetchCompletedDates()
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.upcomingDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupUpcomingDates(it.value)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.completedDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupCompletedDates(it.value)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.inviteeDatesRequiringApproval.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupInviteeDates(it.value)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.dateInterestsRequiringApproval.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupDateInterests(it.value)
                }

                is Resource.Failure -> {}
            }
        }

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

        viewModel.getSubscriptionStatusForHome.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    sharedPrefHelper.saveBoolean(IS_FREE_PLAN, it.value.isFreeTrial)
                }

                is Resource.Failure -> {
                    //
                }
            }
        }
    }

    // Override onActivityResult to handle the result of the calendar activity
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("RESULTCODEK", "$requestCode")
        if (requestCode == ADD_EVENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // The user successfully added the event to the calendar
                Log.d("OKKKKK", "Event added to calendar")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation
                Log.d("NNNNOKKKKK", "Event addition canceled")
            }
        }
    }

    private fun setupDateInterests(dates: DateInterestsRequiringApproval) {
        if (dates.isEmpty()) {
            binding.datesRequiringAppLyt.visibility = View.GONE
            binding.dateInterestRecycler.visibility = View.GONE
        } else {
            binding.datesRequiringAppLyt.visibility = View.VISIBLE
            binding.dateInterestRecycler.visibility = View.VISIBLE
            val adapter =
                mixpanel?.let {
                    DateInterestsRequiringApprovalRecyclerAdapter(
                        dates,
                        viewModel,
                        it,
                    )
                }
            binding.dateInterestRecycler.adapter = adapter
        }
    }

    private fun setupInviteeDates(dates: InviteeDatesRequiringApproval) {
        if (dates.isEmpty()) {
            binding.approvedDatesLyt.visibility = View.GONE
            binding.approvedDatesLytTxt.visibility = View.GONE
            binding.approvedDatesLytView.visibility = View.GONE
        } else {
            binding.approvedDatesLyt.visibility = View.VISIBLE
            binding.approvedDatesLytTxt.visibility = View.VISIBLE
            binding.approvedDatesLytView.visibility = View.VISIBLE
            val adapter =
                mixpanel?.let { InviteeDateForApprovalRecyclerAdapter(dates, viewModel, ctx, it) }
            binding.inviteeDatesRecycler.adapter = adapter
        }
    }

    private fun setupCompletedDates(dates: CompletedDateResponse) {
        if (dates.isEmpty()) {
            binding.datesCompletedAppLyt.visibility = View.GONE
            binding.completedDatesTxt.visibility = View.GONE
            binding.completedDatesDivider.visibility = View.GONE
        } else {
            binding.datesCompletedAppLyt.visibility = View.VISIBLE
            binding.completedDatesTxt.visibility = View.VISIBLE
            binding.completedDatesDivider.visibility = View.VISIBLE
            val adapter =
                mixpanel?.let {
                    CompletedDatesRecyclerAdapter(
                        viewModel,
                        viewLifecycleOwner,
                        requireContext(),
                        dates,
                        it,
                    )
                }
            binding.completedDatesRecycler.adapter = adapter
        }
    }

    private fun setupUpcomingDates(dates: UpcomingDates) {
        if (dates.isEmpty()) {
            binding.upcomingDatesRecycler.visibility = View.GONE
            binding.noUpcomingDates.visibility = View.VISIBLE
        } else {
            binding.upcomingDatesRecycler.visibility = View.VISIBLE
            binding.noUpcomingDates.visibility = View.GONE
            val adapter = UpcomingDatesRecyclerAdapter(requireActivity(), this, dates, ctx)
            binding.upcomingDatesRecycler.adapter = adapter
        }
    }

    override fun onCalendarEventAdded(
        resultCode: Int,
        data: Intent?,
    ) {
        if (resultCode == Activity.RESULT_OK) {
            mixpanel?.track("Android_Home_Added_Date_to_Google_Calendar")
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // The user canceled the operation
//            Log.d("NNNNOKKKKK","Event addition canceled")
        }
    }

    override fun startActivityForCalendarEvent(
        intent: Intent,
        resultCode: Int,
    ) {
        Log.d("RESULTCODEK", "$resultCode")
        startActivityForResult(intent, ADD_EVENT_REQUEST_CODE)
    }

    override fun onDestroy() {
        mixpanel?.flush()
        mixpanel?.optOutTracking()
        super.onDestroy()
    }
}
