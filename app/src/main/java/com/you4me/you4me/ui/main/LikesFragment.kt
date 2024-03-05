package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.you4me.you4me.databinding.FragmentLikesBinding
import com.you4me.you4me.models.FetchDateInterest
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.OnSwipeTouchListener

class LikesFragment : BaseFragment<MainViewModel, FragmentLikesBinding, MainRepository>() {

    private var dateInterests = FetchDateInterest()
    private var currentIdx = -1

    private lateinit var mediaControls: MediaController

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentLikesBinding {
        return FragmentLikesBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        viewModel.fetchDateInterests()
        mediaControls = MediaController(ctx)
        mediaControls.setAnchorView(binding.userVideo)
        mediaControls.setMediaPlayer(binding.userVideo)
        binding.userVideo.setMediaController(mediaControls)

        binding.acceptBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dateInterests[currentIdx]
            viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
        }
        binding.rejectBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dateInterests[currentIdx]
            viewModel.rejectDateInterest(
                d.interestID,
                d.dateID, "REJECTED"
            )
        }

        binding.mainLyt.setOnTouchListener(object : OnSwipeTouchListener(ctx) {
            override fun onSwipeLeft() {
                view?.performClick()
                super.onSwipeLeft()
                if (currentIdx < 0) return
                val d = dateInterests[currentIdx]
                showLoading(true)
                viewModel.rejectDateInterest(
                    d.interestID,
                    d.dateID, "REJECTED"
                )
            }


            override fun onSwipeRight() {
                view?.performClick()
                super.onSwipeRight()
                if (currentIdx < 0) return
                showLoading(true)
                val d = dateInterests[currentIdx]
                viewModel.updateDateInterest(d.interestID, d.dateID, "PENDING_TIME_APPROVAL")
            }
        })
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) {viewModel.getSubscriptionStatus()}
        viewModel.getSubscriptionStatus.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isFreeTrial || it.value.isPremium) {
                        setupView()
                    } else {
                        showDialog("You need to subscribe to access this screen", false)
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.fetchDateInterests.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) showEmpty()
                    else {
                        dateInterests = it.value
                        setScreen()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.rejectDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dateInterests.lastIndex) setScreen()
                    else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }

        viewModel.updateDateInterest.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dateInterests.lastIndex) setScreen()
                    else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showDialog(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setScreen() {
        currentIdx++
        val date = dateInterests[currentIdx]

        binding.userName.text = "${date.name}, ${date.age}"
        val videoUrI = Uri.parse(date.videoURL)
        binding.userVideo.setVideoURI(videoUrI)
        binding.userVideo.start()
    }

    private fun showEmpty() {
        binding.mainLyt.visibility = View.GONE
        binding.emptyLyt.visibility = View.VISIBLE
    }

    private fun showLoading(loading: Boolean) {
        binding.mainLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.emptyLyt.visibility = if (loading) View.GONE else View.VISIBLE
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }
}