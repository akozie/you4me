package com.you4me.you4me.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.you4me.you4me.R
import com.you4me.you4me.databinding.SentRequestsItemBinding
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.network.Resource
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.ui.main.SuggestNewDateDialog

class SentRequestsRecyclerAdapter(
    private val dates: InviteeDatesRequiringApproval, // Use MutableList for better handling
    private val viewModel: MainViewModel,
    private val context: Context,
    private val mixpanelAPI: MixpanelAPI,
    private val fragmentManager: FragmentManager,
    private val lifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<SentRequestsRecyclerAdapter.MyViewHolder>() {
    // Store fetched images locally to persist between screen changes
    private val imageCache = mutableMapOf<String, String>()

    class MyViewHolder(val binding: SentRequestsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val v = SentRequestsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dates.size // Ensure proper item count
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        if (dates.isEmpty()) {
            showEmpty(holder.binding)
            return
        }

        val date = dates[position]
        holder.binding.apply {
            userName.text = date.name
            location.text = date.place
            availableDate.text = "${date.date} : ${date.time}"

            // Clear previous image before loading
            Glide.with(context).clear(imageView)

            // Load image from cache or fetch if not available
            if (imageCache.containsKey(date.userId)) {
                loadImage(imageCache[date.userId] ?: "", this)
            } else {
                getImagesResponse(date.userId, this)
            }

            suggestANewDate.setOnClickListener {
                val dialog =
                    SuggestNewDateDialog(date.date, date.time) { newDate, newTime ->
                        showLoading(true, this)

                        viewModel.proposeNewDateTime(date.dateId, date.interestId, newDate, newTime)

                        // Remove item from list and refresh UI
                        dates.removeAt(position)
                        notifyDataSetChanged()
                        mixpanelAPI.track("Android_Sent_Request_Proposed_New_Date_Time")
                        showEmpty(this) // Ensure empty UI updates
                        Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                        fragmentManager.popBackStack()
                    }
                dialog.show(fragmentManager, "SuggestNewDateDialog")
            }

            acceptDate.setOnClickListener {
                mixpanelAPI.track("Android_Sent_Request_Accept_Date_Button_Clicked")
                showLoading(true, this)

                viewModel.updateDateInterest(date.interestId, date.dateId, "APPROVED")

                // Remove item and refresh UI
                dates.removeAt(position)
                notifyDataSetChanged()

                showEmpty(this) // Ensure empty UI updates
                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                fragmentManager.popBackStack()
            }

            reportDate.setOnClickListener {
                showReportAbuseDialog(date.dateId, date.userId, date.interestId, position)
            }
        }
    }

    private fun showReportAbuseDialog(
        dateId: String,
        userId: String,
        interestID: String,
        position: Int,
    ) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report_abuse, null)

        // Initialize UI elements
        val etFeedback = dialogView.findViewById<EditText>(R.id.et_feedback)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)

        // Create AlertDialog
        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(true)
                .create()

        // Handle submit button click
        btnSubmit.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            mixpanelAPI.track("Android_Sent_Request_Report_Date_Button_Clicked")
            val obj =
                JsonObject().apply {
                    addProperty("date_id", dateId)
                    addProperty("user_id", userId)
                    addProperty("thumbs_up", false)
                    addProperty("comment", feedback)
                }
            Toast.makeText(context, "Report Submitted", Toast.LENGTH_SHORT).show()
            viewModel.updateReview(obj)
            viewModel.updateDateInterest(interestID, dateId, "ABUSE_REPORTED")
            // Remove item and refresh UI
            dates.removeAt(position)
            notifyDataSetChanged()
            dialog.dismiss() // Close the dialog
        }

        // Show the dialog
        dialog.show()
    }

    private fun getImagesResponse(
        userId: String,
        holder: SentRequestsItemBinding,
    ) {
        viewModel.getImagesAndVideos(userId)
        viewModel.getImagesAndVideos.observe(lifecycleOwner) { resource ->
            showLoading(false, holder)
            when (resource) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = resource.value
                    val firstImageUrl = listOfImagesAndVideos.firstOrNull { it.category == "image" }?.fileURL
                    if (!firstImageUrl.isNullOrEmpty()) {
                        val secureUrl = firstImageUrl.replace("http://", "https://")
                        imageCache[userId] = secureUrl // Store URL in cache
                        loadImage(secureUrl, holder)
                    } else {
                        showEmpty(holder)
                    }
                }
                is Resource.Failure -> {
                    Log.e("Adapter", "Failed to load images")
                }
            }
        }
    }

    private fun loadImage(
        url: String,
        holder: SentRequestsItemBinding,
    ) {
        Glide.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Ensures caching
            .into(holder.imageView)
    }

    private fun showLoading(
        loading: Boolean,
        holder: SentRequestsItemBinding,
    ) {
        holder.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showEmpty(holder: SentRequestsItemBinding) {
        if (dates.isEmpty()) {
            holder.constraintLayout2.visibility = View.VISIBLE
            holder.mainLyt.visibility = View.GONE
            holder.loader.visibility = View.GONE
        }
    }
}
