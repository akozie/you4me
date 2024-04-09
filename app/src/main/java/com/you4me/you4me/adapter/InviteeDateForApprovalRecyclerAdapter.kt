package com.you4me.you4me.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.utils.Utils
import com.you4me.you4me.databinding.ApprovedDateItemBinding
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.ui.main.MainViewModel
import java.util.Calendar

class InviteeDateForApprovalRecyclerAdapter(
    private val dates: InviteeDatesRequiringApproval,
    private val viewModel: MainViewModel,
    private val context: Context
) : RecyclerView.Adapter<InviteeDateForApprovalRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ApprovedDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = ApprovedDateItemBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return MyViewHolder(v)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.apply {
            name.text = date.name
            location.text = date.place
            dateODate.text = "${date.date} : ${date.time}"

            acceptBtn.setOnClickListener {
                //accept
                //update list and ui
                viewModel.updateDateInterest(date.interestId, date.dateId, "APPROVED")
                dates.removeAt(position)
                notifyItemRemoved(position)
            }
            newTimeBtn.setOnClickListener {
                acceptBtn.visibility = View.GONE
                newTimeBtn.visibility = View.GONE
                newDateTimeLyt.visibility = View.VISIBLE
            }
            updateTimeBtn.setOnClickListener {
                //propose new time
                //update list and ui
                viewModel.proposeNewDateTime(
                    date.dateId,
                    date.interestId,
                    newDate.text.toString(),
                    newTime.text.toString()
                )
                dates.removeAt(position)
                notifyItemRemoved(position)
            }

            val time = TimePickerDialog.OnTimeSetListener { timePicker, i, i2 ->
                val hour = i.toString().padStart(2, '0')
                val minute = i.toString().padStart(2, '0')
                newTime.text = "$hour:$minute"
            }

            newTime.setOnClickListener {
                TimePickerDialog(context, time, 12, 0, true).show()
            }

            val calendar = Calendar.getInstance()
            newDate.text = Utils.getDateFormat().format(calendar.time)
            newTime.text = "12:00"
            val datee = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                newDate.text = Utils.getDateFormat().format(calendar.time)
            }

            newDate.setOnClickListener {
                DatePickerDialog(
                    context,
                    datee,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }
}