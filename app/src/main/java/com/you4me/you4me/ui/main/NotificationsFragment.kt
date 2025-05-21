package com.you4me.you4me.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.you4me.you4me.adapter.NotificationsRecyclerAdapter
import com.you4me.you4me.databinding.FragmentNotificationsBinding
import com.you4me.you4me.`interface`.OnNotificationClickListener
import com.you4me.you4me.models.Notification
import com.you4me.you4me.models.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.base.BaseFragment
import com.you4me.you4me.ui.main.messaging.model.ChatMessage
import com.you4me.you4me.utils.Constants
import com.you4me.you4me.utils.safeNavigate
import com.you4me.you4me.utils.safeNavigateUp
import java.text.SimpleDateFormat
import java.util.*

class NotificationsFragment :
    BaseFragment<MainViewModel, FragmentNotificationsBinding, MainRepository>("NOTIFICATION") {
    private lateinit var user: User

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ) = FragmentNotificationsBinding.inflate(layoutInflater)

    override fun getRepository() =
        MainRepository(
            dataSource.buildApi(ApiCollector::class.java),
        )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)

        mixpanel?.track("Android_Notification_Viewed")

        binding.loader.show()

        viewModel.user.observe(viewLifecycleOwner) { viewModel.getNotifications() }

        viewModel.getNotificationsResponse.observe(viewLifecycleOwner) { resource ->
            binding.loader.hide()
            when (resource) {
                is Resource.Success -> {
                    if (resource.value.isEmpty()) {
                        showEmptyState()
                    } else {
                        setupRecycler(resource.value)
                    }
                }

                is Resource.Failure ->
                    showToast(
                        resource.message ?: resource.errorBody ?: "Error: ${
                            resource.errorCode
                                ?: ""
                        }",
                    )
            }
        }
        // Set up the Toolbar using View Binding
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        // Enable the up button (back arrow)
        (activity as? AppCompatActivity)?.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            // Set the title
            title = "Notifications"
        }
        binding.toolbar.setNavigationOnClickListener(
            View.OnClickListener
                { findNavController().safeNavigateUp() },
        )
    }

    private fun setupRecycler(notifications: ArrayList<Notification>) {
        val sortedNotifications =
            notifications.sortedByDescending {
                SimpleDateFormat("yyyy/MM/ddhh:mm", Locale.getDefault()).parse(
                    "${it.date}${it.time}",
                )
            }

        val adapter =
            NotificationsRecyclerAdapter(
                user = user,
                sortedNotifications,
                this,
                object : OnNotificationClickListener {
                    override fun onNotificationClick(notification: Notification) {
                        if (notification.seen == "false") {
                            viewModel.markNotificationAsRead(notification.notify_id)
                            handleNotificationClick(notification)
                        }
                        if (notification.category == "CHAT") {
                            viewModel.getUpcomingDates(notification.user_id)
                            viewModel.upcomingDates.observe(viewLifecycleOwner) {
                                when (it) {
                                    is Resource.Success -> {
                                        val result = it.value
                                        val dateAndTime = "${result[0].date} ${result[0].time}"
                                        val chatMessage =
                                            ChatMessage(
                                                dateId = result[0].dateId,
                                                senderId = user.userId,
                                                senderName = user.name,
                                                recipientId = result[0].userId,
                                                recipientName = result[0].name,
                                            )
                                        val action =
                                            NotificationsFragmentDirections.actionNotificationsFragmentToChatFragment(chatMessage)
                                        findNavController().safeNavigate(action)
                                    }

                                    is Resource.Failure -> {
                                        showToast(it.message ?: it.errorBody ?: "")
                                    }
                                }
                            }
                        }
                    }
                },
            )
        binding.notificationsRecyclerView.adapter = adapter
    }

    private fun handleNotificationClick(notification: Notification) {
        val action =
            when (notification.category) {
                Constants.NotificationConstants.Categories.DATE_REQUEST,
                Constants.NotificationConstants.Categories.DATE_MEETUP,
                -> {
                    NotificationsFragmentDirections
                        .actionNotificationsFragmentToNotificationViewFragment(
                            timestamp = "${notification.date} ${notification.time}",
                            notificationText = notification.message,
                        )
                }

                Constants.NotificationConstants.Categories.DATE_PROPOSAL,
                Constants.NotificationConstants.Categories.DATE_CREATOR_ACCEPTED_INTEREST,
                Constants.NotificationConstants.Categories.NEW_DATE_INTEREST,
                -> {
                    NotificationsFragmentDirections
                        .actionNotificationsFragmentToDatesFragment(notification.user_id)
                }
                else -> {
                    Log.e(
                        "NotificationHandler",
                        "Unknown category:" +
                            " ${notification.category}",
                    )
                    return
                }
            }
        findNavController().safeNavigate(action)
    }

//    private fun checkTimeAndShowToast(dateTimeString: String): Boolean {
//        // Define the correct formatter for "2025/02/14 08:56"
//        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
//
//        // Parse the given date-time string
//        val givenDateTime = LocalDateTime.parse(dateTimeString, formatter)
//
//        // Get the current time
//        val currentDateTime = LocalDateTime.now()
//
//        // Calculate the difference in hours
//        val difference = Duration.between(currentDateTime, givenDateTime).toHours()
//
//        // Return true if the difference is less than 3 hours
//        return Math.abs(difference) < 3
//    }

    private fun showEmptyState() {
        binding.apply {
            lottieAnimationView.setAnimation("not-found.json")
            lottieAnimationView.playAnimation()
            constraintLayout2.visibility = ViewGroup.VISIBLE
            notificationsRecyclerView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mixpanel?.mixpanel?.flush()
        mixpanel?.mixpanel?.optOutTracking()
    }
}
