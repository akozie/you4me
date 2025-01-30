package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.you4me.you4me.adapter.SentRequestsRecyclerAdapter
import com.you4me.you4me.databinding.FragmentSentRequestsBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.SharedPrefHelper

class SentRequestsFragment : BaseFragment<MainViewModel, FragmentSentRequestsBinding, MainRepository>() {
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSentRequestsBinding {
        return FragmentSentRequestsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        showLoading(true)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        val user = gson.fromJson(userProfile, User::class.java)

        viewModel.getInviteeDatesRequiringApproval(user.userId)

        viewModel.inviteeDatesRequiringApproval.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    setupInviteeDates(it.value)
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setupInviteeDates(dates: InviteeDatesRequiringApproval) {
        if (dates.isEmpty()) {
            showEmpty()
        } else {
            val adapter = SentRequestsRecyclerAdapter(dates, viewModel, ctx, requireActivity().supportFragmentManager, viewLifecycleOwner)
            binding.upcomingDatesRecycler.adapter = adapter
        }
    }

    private fun showEmpty() {
        binding.constraintLayout2.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun showLoading(loading: Boolean) {
        binding.cardView.visibility = if (loading) View.GONE else View.VISIBLE
        binding.view.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
//        billingManager.endConnection()
    }
}
