package com.you4me.you4me.adapter.finddates

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.you4me.you4me.R
import com.you4me.you4me.models.ImagesVideosResponseItem

class ImagePagerAdapter(
    private val imageList: List<ImagesVideosResponseItem>,
    private val onImageClick: (ImagesVideosResponseItem, Int) -> Unit
) : RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {
        init {
            imageView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onImageClick(imageList[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            isClickable = true
            isFocusable = true
            clipToOutline = true
            background = ContextCompat.getDrawable(context, R.drawable.rounded_bg_img)
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageItem = imageList[position]

        Glide.with(holder.imageView.context)
            .load(imageItem.fileURL)
            .placeholder(R.drawable.find_date_placeholder)
            .error(R.drawable.find_date_placeholder)
            .transform(RoundedCorners(20.dpToPx(holder.imageView.context)))
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageList.size
}

// Extension to convert dp to pixels
fun Int.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()