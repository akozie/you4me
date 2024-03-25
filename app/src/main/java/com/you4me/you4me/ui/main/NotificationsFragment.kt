package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.you4me.you4me.adapter.NotificationsRecyclerAdapter
import com.you4me.you4me.databinding.FragmentNotificationsBinding
import com.you4me.you4me.models.Notification
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.base.ViewModelFactory
import java.text.SimpleDateFormat
import java.time.LocalDate

class NotificationsFragment :
    BaseFragment<MainViewModel, FragmentNotificationsBinding, MainRepository>() {
    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotificationsBinding {
        return FragmentNotificationsBinding.inflate(layoutInflater)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.user.observe(viewLifecycleOwner) {
            viewModel.getNotifications(it.userId)
        }

        viewModel.getNotificationsResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.isEmpty()) showEmpty()
                    else {
//                        val notification = it.value
//                        notification.reverse()
                        setupRecycler(it.value)
                    }
                }
                is Resource.Failure -> {

                }
            }
        }
    }

    private fun setupRecycler(notifications : ArrayList<Notification>) {
        val dateFormat = SimpleDateFormat("yyyy/MM/ddhh:mm")

        val n = notifications.
        sortedByDescending {
            dateFormat.parse("${it.date}${it.time}")
        }
        val adapter = NotificationsRecyclerAdapter(n, this, viewModel)
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun showEmpty() {
        binding.emptyLyt.visibility = ViewGroup.VISIBLE
        binding.notificationsRecyclerView.visibility = View.GONE
    }


}