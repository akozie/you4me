package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import com.you4me.you4me.R
import com.you4me.you4me.adapter.SentRequestsRecyclerAdapter
import com.you4me.you4me.databinding.FragmentSentRequestsBinding
import com.you4me.you4me.models.*
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment

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

    private fun startTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
    }

    private fun startSecondTiltAnimation(isRightSwipe: Boolean) {
        val tiltAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.second_tilt_animation)
        if (isRightSwipe) {
            tiltAnimation.interpolator = AccelerateDecelerateInterpolator()
        } else {
            tiltAnimation.interpolator = DecelerateInterpolator()
        }
    }

    private fun setupObservers() {
        showLoading(true)
        viewModel.getInviteeDatesRequiringApproval()

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
            val adapter = SentRequestsRecyclerAdapter(dates, viewModel, ctx, requireActivity().supportFragmentManager)
            binding.upcomingDatesRecycler.adapter = adapter
        }
    }

    private fun showEmpty() {
        binding.cardView.visibility = View.GONE
//        binding.mainLytBtn.visibility = View.GONE

        // Get the current layout parameters
        val layoutParams = binding.cardView.layoutParams as? ViewGroup.MarginLayoutParams

        // Check if the cast was successful
        layoutParams?.let {
            it.bottomMargin = 0
            binding.cardView.layoutParams = it
        }

        binding.constraintLayout2.visibility = View.VISIBLE
//        binding.emptyLyt.visibility = View.VISIBLE
        binding.loader.visibility = View.GONE
    }

    private fun showLoading(loading: Boolean) {
        binding.cardView.visibility = if (loading) View.GONE else View.VISIBLE
//        binding.mainLytBtn.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
//        billingManager.endConnection()
    }
}
