package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.you4me.you4me.adapter.NotificationsRecyclerAdapter
import com.you4me.you4me.databinding.FragmentNotificationsBinding
import com.you4me.you4me.models.Notification
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NotificationsFragment :
    BaseFragment<MainViewModel, FragmentNotificationsBinding, MainRepository>("NOTIFICATION") {
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentNotificationsBinding {
        return FragmentNotificationsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.loader.show()
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getNotifications()
        }
        mixpanel?.track("Android_Notification_Viewed")
        viewModel.getNotificationsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.loader.hide()
                    if (it.value.isEmpty()) {
                        showEmpty()
                    } else {
                        setupRecycler(it.value)
                    }
                }
                is Resource.Failure -> {
                    showToast(it.message ?: it.errorBody ?: "Error: ${it.errorCode ?: ""}")
                }
            }
        }
    }

    private fun setupRecycler(notifications: ArrayList<Notification>) {
        val dateFormat = SimpleDateFormat("yyyy/MM/ddhh:mm")

        val n =
            notifications
                .sortedByDescending {
                    dateFormat.parse("${it.date}${it.time}")
                    //  formatDate("${it.date}", "${it.time}")
                }
        val adapter = NotificationsRecyclerAdapter(n, this, viewModel)
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun showEmpty() {
        binding.lottieAnimationView.setAnimation("not-found.json")
        binding.lottieAnimationView.playAnimation()
        binding.constraintLayout2.visibility = ViewGroup.VISIBLE
        binding.notificationsRecyclerView.visibility = View.GONE
    }

    override fun onDestroy() {
        mixpanel?.flush()
        mixpanel?.optOutTracking()
        super.onDestroy()
    }
}
