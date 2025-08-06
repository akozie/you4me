package com.you4me.you4me.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.you4me.you4me.R

class FullScreenImagePagerAdapter(
    private val imageUrls: List<String>
) : RecyclerView.Adapter<FullScreenImagePagerAdapter.FullScreenImageViewHolder>() {

    inner class FullScreenImageViewHolder(val photoView: ImageView) :
        RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FullScreenImageViewHolder {
        val photoView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
        }
        return FullScreenImageViewHolder(photoView)
    }

    override fun onBindViewHolder(holder: FullScreenImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        Glide.with(holder.photoView.context)
            .load(imageUrl)
            .placeholder(R.drawable.find_date_placeholder)
            .error(R.drawable.find_date_placeholder)
            .into(holder.photoView)
    }

    override fun getItemCount(): Int = imageUrls.size
}