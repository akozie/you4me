package com.you4me.you4me.adapter

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.UpcomingDateItemBinding
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.models.User
import com.you4me.you4me.ui.main.HomeFragmentDirections
import com.you4me.you4me.ui.main.messaging.model.ChatMessage
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class UpcomingDatesRecyclerAdapter(private val user: User, private val fragment: Fragment, private val listener: CalendarResultListener, private val dates: UpcomingDates, private val context: Context) : RecyclerView.Adapter<UpcomingDatesRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: UpcomingDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val binding = UpcomingDateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        val date = dates[position]
        holder.binding.text.text = "You have a date with ${date.name} at ${date.place} on ${date.date} by ${date.time}"
        holder.binding.addToCalender.setOnClickListener {
            // add to calendar
            val intent = Intent(Intent.ACTION_EDIT)
            intent.setType("vnd.android.cursor.item/event")
            val outputFormat = "yyyy-MM-dd"
            val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
            val parsedDate = LocalDate.parse(date.date, dateFormatter)
            val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern(outputFormat))
            val dateTime = LocalDateTime.parse("${formattedDate}T${date.time}")
            addEventToCalendar("Date with ${date.name}", date.place, dateTime)
        }
        holder.binding.chat.setOnClickListener {
            val chatMessage =
                ChatMessage(
                    dateId = date.dateId,
                    senderId = user.userId,
                    senderName = user.name,
                    recipientId = date.dateId,
                    recipientName = date.name,
                )
            val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(chatMessage)
            fragment.findNavController().navigate(action)
        }
    }

    private fun addEventToCalendar(
        title: String,
        location: String,
        dateTime: LocalDateTime,
    ) {
        val epochMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent =
            Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, epochMillis)
                putExtra(CalendarContract.Events.ALL_DAY, false)
            }
        listener.startActivityForCalendarEvent(intent, ADD_EVENT_REQUEST_CODE)
    }

    interface CalendarResultListener {
        fun onCalendarEventAdded(
            resultCode: Int,
            data: Intent?,
        )

        fun startActivityForCalendarEvent(
            intent: Intent,
            resultCode: Int,
        )
    }
}
