package com.you4me.you4me.adapter

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.you4me.you4me.databinding.ApprovedDateItemBinding
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.utils.Utils
import java.util.Calendar

class InviteeDateForApprovalRecyclerAdapter(
    private val dates: InviteeDatesRequiringApproval,
    private val viewModel: MainViewModel,
    private val context: Context,
    private val mixpanelAPI: MixpanelAPI,
) : RecyclerView.Adapter<InviteeDateForApprovalRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: ApprovedDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val v = ApprovedDateItemBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        // Check if the dates array is not empty
        return if (dates.isNotEmpty()) {
            // Always return 1 (the count of the first element)
            1
        } else {
            // If the array is empty, return 0 or handle accordingly
            0
        }
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        val date = dates[position]
        holder.binding.apply {
             userName.text = date.name
//            location.text = "Cheers! Your date with ${date.name} is scheduled for ${date.date} at ${date.time}. Does this date and time work for you?"
//             dateODate.text = "${date.date} : ${date.time}"

            acceptBtn.setOnClickListener {
                // accept
                mixpanelAPI.track("Android_Home_Approve_Date_Invitee_Button_Pressed")
                // update list and ui
                viewModel.updateDateInterest(date.interestId, date.dateId, "APPROVED")
                dates.clear()
//                dates.removeAt(position)
                notifyItemRemoved(position)
            }
            rejectBtn.setOnClickListener {
                acceptBtn.visibility = View.GONE
//                newTimeBtn.visibility = View.GONE
//                newDateTimeLyt.visibility = View.VISIBLE
                mixpanelAPI.track("Android_Home_Propose_Time_Button_Pressed")
            }
//            updateTimeBtn.setOnClickListener {
//                // propose new time
//                // update list and ui
//                viewModel.proposeNewDateTime(
//                    date.dateId,
//                    date.interestId,
//                    newDate.text.toString(),
//                    newTime.text.toString(),
//                )
//                dates.clear()
////                dates.removeAt(position)
//                notifyItemRemoved(position)
//            }

            val time =
                TimePickerDialog.OnTimeSetListener { timePicker, hourOfDay, minute ->
                    val hour = hourOfDay.toString().padStart(2, '0')
                    val minutePadded = minute.toString().padStart(2, '0')
//                    newTime.text = "$hour:$minutePadded"
                }

            rejectBtn.setOnClickListener {
                TimePickerDialog(context, time, 12, 0, true).show()
            }

            val calendar = Calendar.getInstance()
//            newDate.text = Utils.getDateFormat().format(calendar.time)
//            newTime.text = "12:00"
            val datee =
                DatePickerDialog.OnDateSetListener { _, year, month, day ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, day)
//                    newDate.text = Utils.getDateFormat().format(calendar.time)
                }

//            newDate.setOnClickListener {
//                DatePickerDialog(
//                    context,
//                    datee,
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH),
//                ).show()
//            }
        }
    }
}
