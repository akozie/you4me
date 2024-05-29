package com.you4me.you4me.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.you4me.you4me.databinding.DateInterestRequiringApprovalItemBinding
import com.you4me.you4me.models.DateInterestsRequiringApproval
import com.you4me.you4me.ui.main.MainViewModel

class DateInterestsRequiringApprovalRecyclerAdapter(private val dates : DateInterestsRequiringApproval, private val viewModel: MainViewModel) : RecyclerView.Adapter<DateInterestsRequiringApprovalRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding : DateInterestRequiringApprovalItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = DateInterestRequiringApprovalItemBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return MyViewHolder(v)
    }

//    override fun getItemCount() = dates.size

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

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.apply {
            if (date.proposedDate.isNotEmpty() && date.proposedTime.isNotEmpty()){
                txt.text = "${date.name} has proposed a new date and time: ${date.proposedDate}, ${date.proposedTime}. Does this work for you?"
            } else if (date.proposedDate.isNotEmpty()){
                txt.text = "${date.name} has proposed a new date: ${date.proposedDate}. Does this work for you?"
            } else if (date.proposedTime.isNotEmpty()){
                txt.text = "${date.name} has proposed a new time: ${date.proposedTime}. Does this work for you?"
            } else {
                root.visibility = View.GONE
            }
            yesBtn.setOnClickListener {
                //update list and ui
                    viewModel.updateDateInterest(date.interestId, date.dateId, "APPROVED")
                dates.removeAt(position)
                notifyItemRemoved(position)
            }
            noBtn.setOnClickListener {
                //update list and ui
                viewModel.rejectDateInterest(date.interestId, date.dateId, "REJECTED")
                dates.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

}