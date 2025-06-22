package com.you4me.you4me.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.you4me.you4me.R
import com.you4me.you4me.adapter.DateInterestsRequiringApprovalRecyclerAdapter
import com.you4me.you4me.adapter.DateProposalsRecyclerAdapter
import com.you4me.you4me.adapter.InviteeDateForApprovalRecyclerAdapter
import com.you4me.you4me.databinding.FragmentAllDateProposalsBinding
import com.you4me.you4me.databinding.FragmentHomeBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.models.InviteeDatesRequiringApprovalItem
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper


class AllDateProposalsFragment :
    BaseFragment<MainViewModel, FragmentAllDateProposalsBinding, MainRepository>("HOME"){
    private lateinit var user: User


    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentAllDateProposalsBinding {
        return FragmentAllDateProposalsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        viewModel.getUserDetails(user.userId)
        addObserver()
    }

    private fun addObserver() {
        viewModel.existingUser.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    viewModel.getInviteeDatesRequiringApproval(user.userId)

                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.inviteeDatesRequiringApproval.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
//                    setupInviteeDates(it.value)
                    val list =  InviteeDatesRequiringApproval().apply {
                        add(
                            InviteeDatesRequiringApprovalItem(
                                "CHAT",
                                "2025/05/28",
                                "12wqasde",
                                "12wqasde111",
                                "Emmanuel",
                                "Lekki",
                                "28-05-28",
                                "13:20",
                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
                            )
                        )
                        add(
                            InviteeDatesRequiringApprovalItem(
                                "CHAT",
                                "2025/05/28",
                                "12wqasde",
                                "12wqasde111",
                                "Emmanuel",
                                "Lekki",
                                "28-05-28",
                                "13:20",
                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
                            )
                        )
                        add(
                            InviteeDatesRequiringApprovalItem(
                                "CHAT",
                                "2025/05/28",
                                "12wqasde",
                                "12wqasde111",
                                "Emmanuel",
                                "Lekki",
                                "28-05-28",
                                "13:20",
                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
                            )
                        )
                        add(
                            InviteeDatesRequiringApprovalItem(
                                "CHAT",
                                "2025/05/28",
                                "12wqasde",
                                "12wqasde111",
                                "Emmanuel",
                                "Lekki",
                                "28-05-28",
                                "13:20",
                                "a950d1b2-7245-4c03-8fdd-0e6ecc9d01f8"
                            )
                        )
                    }
                    setupInviteeDates(list)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
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
                mixpanel?.mixpanel?.let { DateProposalsRecyclerAdapter(dates, viewModel, ctx, it) }
            binding.inviteeDatesRecycler.adapter = adapter
        }
    }
}