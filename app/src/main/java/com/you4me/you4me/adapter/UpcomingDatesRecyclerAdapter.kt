package com.you4me.you4me.adapter

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.UpcomingDateItemBinding
import com.you4me.you4me.models.UpcomingDates
import java.text.SimpleDateFormat
import java.util.Date

class UpcomingDatesRecyclerAdapter(private val dates: UpcomingDates, private val context: Context) : RecyclerView.Adapter<UpcomingDatesRecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(val binding : UpcomingDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = UpcomingDateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.text.text = "You have a date with ${date.name} at ${date.place} on ${date.date} by ${date.time}"
        holder.binding.addToCalender.setOnClickListener {
            //add to calendar
            val intent = Intent(Intent.ACTION_EDIT);
            intent.setType("vnd.android.cursor.item/event")
            intent.putExtra(CalendarContract.Events.TITLE, "Date with ${date.name}")
            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                convertDateToMilliSeconds(date.rawDate))
            intent.putExtra(CalendarContract.Events.ALL_DAY, false)// periodicity
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, date.place)
            context.startActivity(intent)
        }
    }

    private fun convertDateToMilliSeconds(date : String) : Long {
        return SimpleDateFormat("yyyy/MM/dd").parse(date).time
    }
}