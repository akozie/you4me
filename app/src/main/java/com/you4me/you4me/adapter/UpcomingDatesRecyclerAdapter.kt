package com.you4me.you4me.adapter

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.UpcomingDateItemBinding
import com.you4me.you4me.model.User
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.ui.main.HomeFragmentDirections
import com.you4me.you4me.ui.main.messaging.model.ChatMessage
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import com.you4me.you4me.utils.Utils.showAlertDialog
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class UpcomingDatesRecyclerAdapter(private val user: User, private val fragment: Fragment, private val listener: CalendarResultListener, private val dates: UpcomingDates, private val context: Context) : RecyclerView.Adapter<UpcomingDatesRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: UpcomingDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val binding = UpcomingDateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = dates.dates.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        val date = dates.dates[position]
//        holder.binding.text.text = "You have a date with ${date.name} at ${date.place} on ${date.date} by ${date.time}"
        holder.binding.addToCalender.setOnClickListener {
            // add to calendar
            Toast.makeText(context, "add to calender", Toast.LENGTH_SHORT).show()
//            val intent = Intent(Intent.ACTION_EDIT)
//            intent.setType("vnd.android.cursor.item/event")
//            val outputFormat = "yyyy-MM-dd"
//            val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
//            val parsedDate = LocalDate.parse(date.date, dateFormatter)
//            val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern(outputFormat))
//            val dateTime = LocalDateTime.parse("${formattedDate}T${date.time}")
//            addEventToCalendar("Date with ${date.name}", date.place, dateTime)
        }
//        holder.binding.chat.setOnClickListener {
//            val dateAndTime = "${date.date} ${date.time}"
//            if (checkTimeAndShowToast(dateAndTime)) {
//                val chatMessage =
//                    ChatMessage(
//                        dateId = date.dateId,
//                        senderId = user.userId,
//                        senderName = user.name,
//                        recipientId = date.userId,
//                        recipientName = date.name,
//                    )
//                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(chatMessage)
//                fragment.findNavController().navigate(action)
//            } else {
//                val dialog = showAlertDialog(context, "You can only message this person 3 hours to the agreed date time", "OK", "", {}, {})
//            }
//        }
    }

    private fun checkTimeAndShowToast(dateTimeString: String): Boolean {
        // Define the correct formatter for "February 20, 2025 19:00"
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm", Locale.ENGLISH)

        // Parse the given date-time string
        val givenDateTime = LocalDateTime.parse(dateTimeString, formatter)

        // Get the current time
        val currentDateTime = LocalDateTime.now()

        // Calculate the difference in hours
        val difference = Duration.between(currentDateTime, givenDateTime).toHours()

        // Return true if the difference is less than 3 hours
        return abs(difference) < 3
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
