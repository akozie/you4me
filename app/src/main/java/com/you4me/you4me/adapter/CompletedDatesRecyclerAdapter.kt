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
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.you4me.you4me.R
import com.you4me.you4me.databinding.CompletedDatesItemBinding
import com.you4me.you4me.models.CompletedDateResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.ui.main.MainViewModel

class CompletedDatesRecyclerAdapter(
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val dates: CompletedDateResponse,
    private val mixpanelAPI: MixpanelAPI,
) : RecyclerView.Adapter<CompletedDatesRecyclerAdapter.MyViewHolder>() {
    class MyViewHolder(val binding: CompletedDatesItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): MyViewHolder {
        val binding = CompletedDatesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount() = dates.size

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
        val date = dates[position]
        holder.binding.userName.text = "${date.name}"
        holder.binding.completed.text = "${date.date}"
        holder.binding.location.text = "${date.venue}"
        getImagesResponse(date.personId, holder.binding)

        if (date.hasReview == "true") {
            holder.binding.thumbsDownLayout.visibility = View.GONE
        } else {
            holder.binding.thumbsDownLayout.visibility = View.VISIBLE
        }
        holder.binding.apply {
            dislikeBtn.setOnClickListener {
                showReportAbuseDialog(date.dateId, date.personId, this)
            }

            likeBtn.setOnClickListener {
                val obj =
                    JsonObject().apply {
                        addProperty("date_id", date.dateId)
                        addProperty("user_id", date.personId)
                        addProperty("thumbs_up", true)
                        addProperty("comment", "")
                    }
                viewModel.updateReview(obj)
                viewModel.updateReviewResponse.observe(lifecycleOwner) {
                    when (it) {
                        is Resource.Success -> {
                            thumbsDownLayout.visibility = View.GONE
                            Toast.makeText(context, "Report Submitted", Toast.LENGTH_SHORT).show()
                        }

                        is Resource.Failure -> {
                        }
                    }
                }

                mixpanelAPI.track("Android_Home_Like_Review_Date_Button_Pressed")
            }
        }
    }

    private fun getImagesResponse(
        userId: String,
        holder: CompletedDatesItemBinding,
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
//                        imageCache[userId] = secureUrl // Store URL in cache
                        loadImage(secureUrl, holder)
                    } else {
//                        showEmpty(holder)
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
        holder: CompletedDatesItemBinding,
    ) {
        Glide.with(context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Ensures caching
            .into(holder.imageView)
    }

    private fun showLoading(
        loading: Boolean,
        holder: CompletedDatesItemBinding,
    ) {
        holder.loader.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showReportAbuseDialog(
        dateId: String,
        userId: String,
        holder: CompletedDatesItemBinding,
    ) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_wrong_dates, null)

        // Initialize UI elements
        val etFeedback = dialogView.findViewById<EditText>(R.id.et_feedback)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)

        // Create AlertDialog
        val dialog =
            AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create()

        // Handle submit button click
        btnSubmit.setOnClickListener {
            val feedback = etFeedback.text.toString().trim()
            mixpanelAPI.track("Android_Home_Dislike_Review_Date_Button_Pressed")
            val obj =
                JsonObject().apply {
                    addProperty("date_id", dateId)
                    addProperty("user_id", userId)
                    addProperty("thumbs_up", false)
                    addProperty("comment", feedback)
                }
            viewModel.updateReview(obj)
            viewModel.updateReviewResponse.observe(lifecycleOwner) {
                when (it) {
                    is Resource.Success -> {
                        holder.thumbsDownLayout.visibility = View.GONE
                        Toast.makeText(context, "Report Submitted", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Failure -> {
                    }
                }
            }
            // Remove item and refresh UI
            dialog.dismiss() // Close the dialog
        }
        // Handle submit button click
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }
}
