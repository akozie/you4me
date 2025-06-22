package com.you4me.you4me.models.useroptions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AgeGroup(
    val label: String,
    val value: String
) : Parcelable