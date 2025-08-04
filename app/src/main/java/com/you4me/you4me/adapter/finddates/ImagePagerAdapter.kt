package com.you4me.you4me.adapter.finddates

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.you4me.you4me.R
import com.you4me.you4me.models.ImagesVideosResponseItem

class ImagePagerAdapter(private val imageList: List<ImagesVideosResponseItem>) :
    RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                300.dpToPx(context)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position].fileURL // adjust if property name differs
        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.find_date_placeholder)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageList.size
}

// Extension to convert dp to pixels
fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()
