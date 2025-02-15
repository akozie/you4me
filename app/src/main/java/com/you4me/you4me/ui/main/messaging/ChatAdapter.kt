package com.you4me.you4me.ui.main.messaging

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.R
import com.you4me.you4me.ui.main.messaging.model.Message
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val messages: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_SENT) R.layout.item_message_sent else R.layout.item_message_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChatViewHolder,
        position: Int,
    ) {
        val message = messages[position]
        holder.textViewMessage.text = message.text
        Log.d("CHECKIN", "${message.timestamp}")
        val currentTime = convertTImeFromMilli(message.timestamp)
        holder.time.text = currentTime
        // Show the appropriate icon based on whether the message has been read
        if (message.seen) {
            holder.readStatusIcon.setImageResource(R.drawable.baseline_remove_red_eye_24) // A checkmark or read icon
        } else {
//            holder.readStatusIcon.setImageResource(R.drawable.icon) // A checkmark or read icon
        }
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewMessage: TextView = view.findViewById(R.id.textViewMessage)
        val readStatusIcon: ImageView = view.findViewById(R.id.readStatusIcon)
        val time: TextView = view.findViewById(R.id.timestampTextView)
    }

    private fun convertTImeFromMilli(timestamp: Long): String {
// Create a Date object from the timestamp
        val date = Date(timestamp)

// Create a SimpleDateFormat to format the date to AM/PM format
        val format =
            SimpleDateFormat("hh:mm a", Locale.getDefault()) // "hh:mm a" is for AM/PM format

// Format the date

        return format.format(date)
    }
}
