package com.you4me.you4me.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.NotificationItemBinding
import com.you4me.you4me.`interface`.OnNotificationClickListener
import com.you4me.you4me.model.User
import com.you4me.you4me.models.Notification
import com.you4me.you4me.utils.Utils.formatDate

class NotificationsRecyclerAdapter(
    private val user: User,
    private val notifications: List<Notification>,
    private val context: Fragment,
    private val listener: OnNotificationClickListener,
) : RecyclerView.Adapter<NotificationsRecyclerAdapter.MyViewHolder>() {
    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        val notification = notifications[position]

        holder.binding.notificationText.text = notification.message
        holder.binding.dateTime.text =
            formatDate(
                "${notification.date}",
                "${notification.time}",
            )

        // Set the background color for seen notifications
        if (notification.seen == "true") {
            holder.binding.notificationLyt.setBackgroundColor(
                context.resources.getColor(
                    R.color.white,
                ),
            )
        }

        // Handle click event for marking the notification as read
        holder.binding.root.setOnClickListener {
            listener.onNotificationClick(notification) // Trigger the click callback
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val binding =
            NotificationItemBinding.inflate(
                context.layoutInflater,
                parent,
                false,
            )
        return MyViewHolder(binding)
    }

    class MyViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(
        binding.root,
    )
}
