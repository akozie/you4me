package com.you4me.you4me.models.useroptions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Gender(
    val label: String,
    val value: String
) : Parcelable