package com.you4me.you4me.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImagesVideosResponseItem(
    val category: String,
    val fileURL: String,
    val reason: String,
    val status: String,
    val userId: String,
    val videoId: String,
    val isProfilePhoto: Boolean = false,
) : Parcelable
