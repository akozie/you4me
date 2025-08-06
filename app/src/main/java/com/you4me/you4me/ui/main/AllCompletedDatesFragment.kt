package com.you4me.you4me.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.CompletedDatesRecyclerAdapter
import com.you4me.you4me.databinding.FragmentAllCompletedDatesBinding
import com.you4me.you4me.databinding.FragmentCompletedDatesBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.CompletedDateResponse
import com.you4me.you4me.models.CompletedDateResponseItem
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper


class AllCompletedDatesFragment :
    BaseFragment<MainViewModel, FragmentAllCompletedDatesBinding, MainRepository>("COMPLETED_DATES") {

    private lateinit var user: User


    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAllCompletedDatesBinding {
        return FragmentAllCompletedDatesBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        viewModel.getUserDetails(user.userId)


        addObservers()
        viewModel.completedDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    setupCompletedDates(it.value)

                    val list =  listOf(
                        CompletedDateResponseItem(
                            "CHAT",
                            "March 10,2025 at 10:00am",
                            "12wqasde",
                            "true",
                            "Emmanuel",
                            "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8",
                            "5",
                            "Lagos",
                            "Lekki"
                        ),
                        CompletedDateResponseItem(
                            "CHAT",
                            "March 10,2025 at 10:00am",
                            "12wqasde",
                            "true",
                            "Emmanuel",
                            "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8",
                            "5",
                            "Lagos",
                            "Lekki"
                        ),
                        CompletedDateResponseItem(
                            "CHAT",
                            "March 10,2025 at 10:00am",
                            "12wqasde",
                            "true",
                            "Emmanuel",
                            "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8",
                            "5",
                            "Lagos",
                            "Lekki"
                        ),
                    )
                    setupCompletedDates(list)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }




//        binding.toggleLayout.setOnClickListener {
//            isExpanded = !isExpanded // Toggle state
//
//            if (isExpanded) {
//                binding.completedDatesRecycler.visibility = View.VISIBLE
//                binding.toggleArrow.setImageResource(R.drawable.baseline_keyboard_arrow_up_24) // Change icon
//            } else {
//                binding.completedDatesRecycler.visibility = View.GONE
//                binding.toggleArrow.setImageResource(R.drawable.baseline_keyboard_arrow_down_24) // Change icon
//            }
//        }
    }

    private fun setupCompletedDates(dates: List<CompletedDateResponseItem>) {
        if (dates.isNullOrEmpty()) {
            binding.datesCompletedAppLyt.visibility = View.GONE
            binding.completedDatesRecycler.visibility = View.GONE
//            binding.completedDatesTxt.visibility = View.GONE
//            binding.completedDatesDivider.visibility = View.GONE
        } else {
            binding.datesCompletedAppLyt.visibility = View.VISIBLE
            binding.completedDatesRecycler.visibility = View.VISIBLE
//            binding.completedDatesTxt.visibility = View.VISIBLE
//            binding.completedDatesDivider.visibility = View.VISIBLE
            val adapter =
                mixpanel?.mixpanel?.let {
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

    private fun addObservers() {
        viewModel.existingUser.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    viewModel.fetchCompletedDates()
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