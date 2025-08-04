package com.you4me.you4me.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.UpcomingDatesRecyclerAdapter
import com.you4me.you4me.databinding.FragmentAllUpcomingDatesBinding
import com.you4me.you4me.databinding.FragmentUpcomingDatesBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.models.UpcomingDatesItem
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper
import com.you4me.you4me.utils.Utils


class AllUpcomingDatesFragment :
    BaseFragment<MainViewModel, FragmentAllUpcomingDatesBinding, MainRepository>("UPCOMING_DATES"), UpcomingDatesRecyclerAdapter.CalendarResultListener{

    private lateinit var user: User

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAllUpcomingDatesBinding {
        return FragmentAllUpcomingDatesBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        viewModel.getUserDetails(user.userId)


        addObservers()
        viewModel.upcomingDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    setupUpcomingDates(it.value)
//                    val list =  UpcomingDates().apply {
//                        add(
//                            UpcomingDatesItem(
//                                "CHAT",
//                                "2025-06-28",
//                                "12wqasde",
//                                "12wqasde111",
//                                "Emmanuel",
//                                "Lekki",
//                                "2025-06-28",
//                                "13:20",
//                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
//                            )
//                        )
//                        add(
//                            UpcomingDatesItem(
//                                "CHAT",
//                                "2025-06-28",
//                                "12wqasde",
//                                "12wqasde111",
//                                "Emmanuel",
//                                "Lekki",
//                                "2025-06-28",
//                                "13:20",
//                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
//                            )
//                        )
//
//                        add(
//                            UpcomingDatesItem(
//                                "CHAT",
//                                "2025-06-28",
//                                "12wqasde",
//                                "12wqasde111",
//                                "Emmanuel",
//                                "Lekki",
//                                "2025-06-28",
//                                "13:20",
//                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
//                            )
//                        )
//
//                        add(
//                            UpcomingDatesItem(
//                                "CHAT",
//                                "2025-06-28",
//                                "12wqasde",
//                                "12wqasde111",
//                                "Emmanuel",
//                                "Lekki",
//                                "2025-06-28",
//                                "13:20",
//                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
//                            )
//                        )
//                    }
//                    setupUpcomingDates(list)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }


    private fun setupUpcomingDates(dates: UpcomingDates) {
        if (dates.dates.isEmpty()) {
            binding.upcomingDatesRecycler.visibility = View.GONE
//            binding.noUpcomingDates.visibility = View.VISIBLE
        } else {
            binding.upcomingDatesRecycler.visibility = View.VISIBLE
//            binding.noUpcomingDates.visibility = View.GONE
            val adapter = UpcomingDatesRecyclerAdapter(user, this, this, dates, ctx)
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
        startActivityForResult(intent, Utils.ADD_EVENT_REQUEST_CODE)
    }

    private fun addObservers() {
        viewModel.existingUser.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    viewModel._user.value = it.value
                    viewModel.getUpcomingDates(user.userId)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    override fun onDestroy() {
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
        super.onDestroy()
    }
}