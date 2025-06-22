package com.you4me.you4me.models

import android.widget.ImageView

class ImagesVideosResponse : ArrayList<ImagesVideosResponseItem>()


data class ImageSlotViews(
    val imageView: ImageView,
    val addBadge: ImageView,
    val cancelButton: ImageView
)
