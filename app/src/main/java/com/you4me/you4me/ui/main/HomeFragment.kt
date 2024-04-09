package com.you4me.you4me.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.R
import com.you4me.you4me.adapter.DateInterestsRequiringApprovalRecyclerAdapter
import com.you4me.you4me.adapter.InviteeDateForApprovalRecyclerAdapter
import com.you4me.you4me.adapter.UpcomingDatesRecyclerAdapter
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.models.UpcomingDatesItem
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class HomeFragment : BaseFragment<MainViewModel, FragmentHomeBinding, MainRepository>(),
    UpcomingDatesRecyclerAdapter.CalendarResultListener {
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addObservers()
        binding.notificationIcon.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_notificationsFragment) }
    }

    private fun addObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getUpcomingDates()
            viewModel.getInviteeDatesRequiringApproval()
            viewModel.getDateInterestsRequiringApproval()
            viewModel.getNotifications(it.userId)
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
                    //switch ui
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
                    //switch ui
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.proposeNewDateTime.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    showToast("Date status updated")
                    //switch ui
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.getNotificationsResponse.observe(viewLifecycleOwner) {
            when(it) {
                is Resource.Success -> {
                    val unseenCount = it.value.mapNotNull { n->
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
    }

    // Override onActivityResult to handle the result of the calendar activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_EVENT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // The user successfully added the event to the calendar
                Log.d("OKKKKK", "Event added to calendar")
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation
                Log.d("NNNNOKKKKK","Event addition canceled")
            }
        }
    }
    private fun setupDateInterests(dates: DateInterestsRequiringApproval) {
        if (dates.isEmpty()) {
            binding.datesRequiringAppLyt.visibility = View.GONE
        } else {
            binding.datesRequiringAppLyt.visibility = View.VISIBLE
            val adapter = DateInterestsRequiringApprovalRecyclerAdapter(dates, viewModel)
            binding.dateInterestRecycler.adapter = adapter
        }
    }

    private fun setupInviteeDates(dates: InviteeDatesRequiringApproval) {
        if (dates.isEmpty()) {
            binding.approvedDatesLyt.visibility = View.GONE
        } else {
            binding.approvedDatesLyt.visibility = View.VISIBLE
            val adapter = InviteeDateForApprovalRecyclerAdapter(dates, viewModel, ctx)
            binding.inviteeDatesRecycler.adapter = adapter
        }
    }

    private fun setupUpcomingDates(dates: UpcomingDates) {
        if (dates.isEmpty()) {
            binding.upcomingDatesRecycler.visibility = View.GONE
            binding.noUpcomingDates.visibility = View.VISIBLE
        } else {
            val dummyData = UpcomingDates().apply {
                add(UpcomingDatesItem("Meeting", "2024-03-05", "1", "Team Meeting", "Conference Room", "2024-04-05", "09:00", "1"))
                add(UpcomingDatesItem("Birthday", "2024-04-10", "2", "John's Birthday", "John's House", "2024-04-10", "18:30", "2"))
                add(UpcomingDatesItem("Appointment", "2024-04-15", "3", "Dentist Appointment", "Dentist Clinic", "2024-04-15", "11:00", "3"))
            }
//            val adapter = UpcomingDatesRecyclerAdapter(dummyData, requireContext())
            val adapter = UpcomingDatesRecyclerAdapter(requireActivity(), this, dates, ctx)
            binding.upcomingDatesRecycler.adapter = adapter
        }
    }

    override fun onCalendarEventAdded(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            // The user successfully added the event to the calendar
            Log.d("OKKKKK", "Event added to calendar")
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // The user canceled the operation
            Log.d("NNNNOKKKKK","Event addition canceled")
        }
    }

//    private fun closeApp() {
//        AlertDialog.Builder(requireContext()).setMessage(
//            "Are you sure you want to close the app?"
//        ).setPositiveButton(
//            "Cancel"
//        ) { dialog, _ ->
//            dialog.dismiss()
//        }.setNegativeButton(
//            "Close App"
//        ) { dialog, _ ->
//            dialog.dismiss()
//            startActivity(Intent(requireActivity(), AuthenticationActivity::class.java))
//            if (activity != null) requireActivity().finish()
//        }.setCancelable(true).create().show()
//    }
}