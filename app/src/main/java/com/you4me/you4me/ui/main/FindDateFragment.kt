package com.you4me.you4me.ui.main

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.you4me.you4me.databinding.FragmentFindDateBinding
import com.you4me.you4me.models.FetchDatesResponseItem
import com.you4me.you4me.models.ValueLabelResponse
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.OnSwipeTouchListener
import com.you4me.you4me.utils.fetchDates


class FindDateFragment : BaseFragment<MainViewModel, FragmentFindDateBinding, MainRepository>() {

    private var dates = ArrayList<FetchDatesResponseItem>()
    private var currentIdx = -1

    private lateinit var mediaControls: MediaController
    private var paymentModes : ArrayList<ValueLabelResponse>? = null
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFindDateBinding {
        return FragmentFindDateBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        mediaControls = MediaController(ctx)
        mediaControls.setAnchorView(binding.userVideo)
        mediaControls.setMediaPlayer(binding.userVideo)
        binding.userVideo.setMediaController(mediaControls)

        binding.acceptBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            val d = dates[currentIdx]
            showLoading(true)
            viewModel.addDateInterest(
                d.date,
                d.time,
                d.userId,
            )
        }
        binding.rejectBtn.setOnClickListener {
            if (currentIdx < 0) return@setOnClickListener
            showLoading(true)
            val d = dates[currentIdx]
            viewModel.addSwipe(
                d.dateId,
                d.userId,
                false
            )
        }


        binding.mainLyt.setOnTouchListener(object : OnSwipeTouchListener(ctx) {
            override fun onSwipeLeft() {
                view?.performClick()
                super.onSwipeLeft()
                if (currentIdx < 0) return
                showLoading(true)
                val d = dates[currentIdx]
                viewModel.addSwipe(
                    d.dateId,
                    d.userId,
                    false
                )
            }


            override fun onSwipeRight() {
                view?.performClick()
                super.onSwipeRight()
                if (currentIdx < 0) return
                showLoading(true)
                val d = dates[currentIdx]
                viewModel.addDateInterest(
                    d.date,
                    d.time,
                    d.userId,
                )
            }
        })
    }

    private fun setupObservers() {
        viewModel.paymentModes.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    paymentModes = it.value
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.fetchDates.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty())
                        showEmpty()
                    else {
                        dates = it.value
                        setScreen()
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.fetchDates()
        }
        viewModel.addDateInterest.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    val d = dates[currentIdx]
                    viewModel.addSwipe(
                        d.dateId,
                        d.userId,
                        true
                    )
                }

                is Resource.Failure -> {
                    showLoading(false)
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
        viewModel.addSwipe.observe(viewLifecycleOwner) {
            showLoading(false)
            when (it) {
                is Resource.Success -> {
                    showToast("Success")
                    if (currentIdx < dates.lastIndex) setScreen()
                    else {
                        showToast("No more dates available")
                        showEmpty()
                    }
                }

                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "")
                }
            }
        }
    }

    private fun setScreen() {
        currentIdx++
        val date = dates[currentIdx]

        binding.userName.text = "${date.name}, ${date.age}"
        binding.location.text = date.place
        binding.payment.text = paymentModes?.first { it.value == date.payment}?.label ?: date.payment
        val videoUrI = Uri.parse(date.videoURL.replace("http:", "https:"))
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