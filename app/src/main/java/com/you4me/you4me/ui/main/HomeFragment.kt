package com.you4me.you4me.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.you4me.you4me.adapter.DateInterestsRequiringApprovalRecyclerAdapter
import com.you4me.you4me.adapter.InviteeDateForApprovalRecyclerAdapter
import com.you4me.you4me.adapter.UpcomingDatesRecyclerAdapter
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment


class HomeFragment : BaseFragment<MainViewModel, FragmentHomeBinding, MainRepository>() {
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
    }

    private fun addObservers() {
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getUpcomingDates()
            viewModel.getInviteeDatesRequiringApproval()
            viewModel.getDateInterestsRequiringApproval()
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
    }

    private fun setupDateInterests(dates: DateInterestsRequiringApproval) {
        if (dates.isEmpty()) {
            binding.dateInterestRecycler.visibility = View.GONE
            binding.noDatesInterests.visibility = View.VISIBLE
        } else {
            val adapter = DateInterestsRequiringApprovalRecyclerAdapter(dates, viewModel)
            binding.dateInterestRecycler.adapter = adapter
        }
    }

    private fun setupInviteeDates(dates: InviteeDatesRequiringApproval) {
        if (dates.isEmpty()) {
            binding.inviteeDatesRecycler.visibility = View.GONE
            binding.noApprovedDatesInterests.visibility = View.VISIBLE
        } else {
            val adapter = InviteeDateForApprovalRecyclerAdapter(dates, viewModel, ctx)
            binding.inviteeDatesRecycler.adapter = adapter
        }
    }

    private fun setupUpcomingDates(dates: UpcomingDates) {
        if (dates.isEmpty()) {
            binding.upcomingDatesRecycler.visibility = View.GONE
            binding.noUpcomingDates.visibility = View.VISIBLE
        } else {
            val adapter = UpcomingDatesRecyclerAdapter(dates, ctx)
            binding.upcomingDatesRecycler.adapter = adapter
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