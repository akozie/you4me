package com.you4me.you4me.adapter

import android.view.LayoutInflater
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

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val date = dates[position]
        holder.binding.apply {
            txt.text = "${date.name} has proposed a new date and time: ${date.proposedDate}, ${date.proposedTime}. Does this work for you?"
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