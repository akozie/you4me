package com.you4me.you4me.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.you4me.you4me.databinding.FragmentNotificationViewBinding
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.utils.safeNavigateUp

class NotificationViewFragment : BaseFragment<MainViewModel, FragmentNotificationViewBinding,
    MainRepository>("NOTIFICATION_VIEW") {

    // Arguments
    private var timestamp: String? = null
    private var notificationText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve arguments
        arguments?.let {
            timestamp = it.getString(ARG_TIMESTAMP)
            notificationText = it.getString(ARG_NOTIFICATION_TEXT)
        }
    }

    override fun getViewModel(): Class<MainViewModel> {
        // Return the appropriate ViewModel class
        return MainViewModel::class.java
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNotificationViewBinding {
        // Return the correct ViewBinding for this fragment
        return FragmentNotificationViewBinding.inflate(inflater, container,
            false)
    }

    override fun getRepository() = MainRepository(
        dataSource.buildApi(ApiCollector::class.java)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.timestamp.text = timestamp
        binding.notification.text = notificationText

        // Set up the Toolbar using View Binding
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        // Enable the up button (back arrow)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            // Set the title
            title = "Notifications" // Replace with your desired title
        }
        binding.toolbar.setNavigationOnClickListener(View.OnClickListener
        {  findNavController().safeNavigateUp() })
    }

    companion object {
        private const val ARG_TIMESTAMP = "timestamp"
        private const val ARG_NOTIFICATION_TEXT = "notificationText"
    }
}
