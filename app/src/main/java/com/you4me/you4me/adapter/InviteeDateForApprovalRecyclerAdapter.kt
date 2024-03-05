package com.you4me.you4me.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.ApprovedDateItemBinding
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.ui.main.MainViewModel

class InviteeDateForApprovalRecyclerAdapter(private val dates : InviteeDatesRequiringApproval, private val viewModel: MainViewModel) : RecyclerView.Adapter<InviteeDateForApprovalRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding : ApprovedDateItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = ApprovedDateItemBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return MyViewHolder(v)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.apply{
            name.text = date.name
            location.text = date.place
            dateODate.text = "${date.place} : ${date.time}"

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
                viewModel.proposeNewDateTime(date.dateId, date.interestId, newDate.text.toString(), newTime.text.toString())
                dates.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }
}