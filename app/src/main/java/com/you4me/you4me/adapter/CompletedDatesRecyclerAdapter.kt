package com.you4me.you4me.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.you4me.you4me.databinding.CompletedDatesItemBinding
import com.you4me.you4me.models.CompletedDateResponse
import com.you4me.you4me.network.Resource
import com.you4me.you4me.ui.main.MainViewModel

class CompletedDatesRecyclerAdapter(
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val context: Context,
    private val dates: CompletedDateResponse,
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
}
