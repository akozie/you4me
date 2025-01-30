package com.you4me.you4me.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.you4me.you4me.databinding.SentRequestsItemBinding
import com.you4me.you4me.models.InviteeDatesRequiringApproval
import com.you4me.you4me.network.Resource
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.ui.main.SuggestNewDateDialog

class SentRequestsRecyclerAdapter(
    private val dates: InviteeDatesRequiringApproval,
    private val viewModel: MainViewModel,
    private val context: Context,
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
        val v = SentRequestsItemBinding.inflate(LayoutInflater.from(parent.context), null, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        // Check if the dates array is not empty
        return dates.size
    }

    override fun onBindViewHolder(
        holder: MyViewHolder,
        position: Int,
    ) {
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

//            getImagesResponse(date.userId, this)
            suggestANewDate.setOnClickListener {
                val dialog =
                    SuggestNewDateDialog(date.date, date.time) { newDate, newTime ->
                        println("New date selected: $newDate at $newTime")
                        viewModel.proposeNewDateTime(
                            date.dateId,
                            date.interestId,
                            newDate,
                            newTime,
                        )
                        dates.clear()
                        notifyItemRemoved(position)
                    }
                dialog.show(fragmentManager, "SuggestNewDateDialog")
            }

            acceptDate.setOnClickListener {
                // accept
                viewModel.updateDateInterest(date.interestId, date.dateId, "APPROVED")
                dates.clear()
                notifyItemRemoved(position)
            }
        }
    }

    private fun getImagesResponse(
        userId: String,
        holder: SentRequestsItemBinding,
    ) {
        viewModel.getImagesAndVideos(userId)
        viewModel.getImagesAndVideos.observeForever { resource ->
            when (resource) {
                is Resource.Success -> {
                    val listOfImagesAndVideos = resource.value
                    val firstImageUrl = listOfImagesAndVideos.firstOrNull { it.category == "image" }?.fileURL
                    if (!firstImageUrl.isNullOrEmpty()) {
                        val secureUrl = firstImageUrl.replace("http://", "https://")
                        imageCache[userId] = secureUrl // Store URL in cache
                        loadImage(secureUrl, holder)
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
}
