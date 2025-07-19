package com.you4me.you4me.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.databinding.NotificationItemBinding
import com.you4me.you4me.`interface`.OnNotificationClickListener
import com.you4me.you4me.model.User
import com.you4me.you4me.models.Notification
import com.you4me.you4me.models.NotificationListItem
import com.you4me.you4me.utils.Utils.formatDate

//class NotificationsRecyclerAdapter(
//    private val user: User,
//    private val notifications: List<Notification>,
//    private val context: Fragment,
//    private val listener: OnNotificationClickListener,
//    private val onOptionClick: (Notification, View) -> Unit
//) : RecyclerView.Adapter<NotificationsRecyclerAdapter.MyViewHolder>() {
//    override fun getItemCount() = notifications.size
//
//    override fun onBindViewHolder(
//        holder: MyViewHolder,
//        position: Int,
//    ) {
//        val notification = notifications[position]
//
//        holder.binding.notificationMessage.text = notification.message
//        holder.binding.timeText.text =
//            formatDate(
//                "${notification.date}",
//                "${notification.time}",
//            )
//
//        // Set the background color for seen notifications
//        if (notification.seen == "true") {
//            holder.binding.notificationLyt.setBackgroundColor(
//                context.resources.getColor(
//                    R.color.white,
//                ),
//            )
//        }
//
//        // Handle click event for marking the notification as read
//        holder.binding.root.setOnClickListener {
//            listener.onNotificationClick(notification) // Trigger the click callback
//        }
//
//
//        holder.binding.optionsMenu.setOnClickListener {
//            onOptionClick(notification, it)
//        }
//    }
//
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int,
//    ): MyViewHolder {
//        val binding =
//            NotificationItemBinding.inflate(
//                context.layoutInflater,
//                parent,
//                false,
//            )
//        return MyViewHolder(binding)
//    }
//
//    class MyViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(
//        binding.root,
//    )
//}

class NotificationsRecyclerAdapter(
    private val user: User,
    private val items: List<NotificationListItem>,
    private val context: Fragment,
    private val listener: OnNotificationClickListener,
    private val onOptionClick: (Notification, View) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE_HEADER = 0
        private const val TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is NotificationListItem.DateHeader -> TYPE_DATE_HEADER
            is NotificationListItem.NotificationItem -> TYPE_NOTIFICATION
        }
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE_HEADER -> {
                val view = context.layoutInflater.inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            else -> {
                val binding = NotificationItemBinding.inflate(context.layoutInflater, parent, false)
                NotificationViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is NotificationListItem.DateHeader -> {
                (holder as DateHeaderViewHolder).bind(item)
            }
            is NotificationListItem.NotificationItem -> {
                (holder as NotificationViewHolder).bind(item.notification)
            }
        }
    }

    inner class DateHeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: NotificationListItem.DateHeader) {
            val headerText = view.findViewById<TextView>(R.id.dateHeaderText)
            headerText.text = item.date
        }
    }

    inner class NotificationViewHolder(private val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.notificationMessage.text = notification.message
            binding.timeText.text = formatDate("${notification.date}", "${notification.time}")

            if (notification.seen == "true") {
                binding.notificationLyt.setBackgroundColor(context.resources.getColor(R.color.white))
            }

            binding.root.setOnClickListener {
                listener.onNotificationClick(notification)
            }

            binding.optionsMenu.setOnClickListener {
                onOptionClick(notification, it)
            }
        }
    }
}
