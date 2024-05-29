package com.you4me.you4me.adapter

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.utils.Utils.formatDate
import com.you4me.you4me.R
import com.you4me.you4me.databinding.NotoficationItemBinding
import com.you4me.you4me.models.Notification
import com.you4me.you4me.ui.main.MainViewModel

class NotificationsRecyclerAdapter(
    private val notifications: List<Notification>,
    private val context: Fragment,
    private val vm: MainViewModel
) : RecyclerView.Adapter<NotificationsRecyclerAdapter.MyViewHolder>() {
    override fun getItemCount() = notifications.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.notificationText.text = notification.message
        holder.binding.dateTime.text = formatDate("${notification.date}", "${notification.time}")
        if (notification.seen == "true") {
            holder.binding.notificationLyt.setBackgroundColor(context.resources.getColor(R.color.white))
        }
        holder.binding.root.setOnClickListener {
            if (notification.seen == "false") {
                vm.markNotificationAsRead(notification.notify_id)
                holder.binding.notificationLyt.setBackgroundColor(context.resources.getColor(R.color.white))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = NotoficationItemBinding.inflate(context.layoutInflater, parent, false)
        return MyViewHolder(binding)
    }

    class MyViewHolder(val binding: NotoficationItemBinding) : RecyclerView.ViewHolder(binding.root)
}