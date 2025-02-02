package com.you4me.you4me.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.UpcomingDateItemBinding
import com.you4me.you4me.models.UpcomingDates
import com.you4me.you4me.utils.Utils.ADD_EVENT_REQUEST_CODE
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class UpcomingDatesRecyclerAdapter(private val activity: Activity, private val listener: CalendarResultListener, private val dates: UpcomingDates, private val context: Context) : RecyclerView.Adapter<UpcomingDatesRecyclerAdapter.MyViewHolder>() {
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
