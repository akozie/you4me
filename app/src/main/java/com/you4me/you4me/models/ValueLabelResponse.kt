package com.you4me.you4me.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ValueLabelResponse(
    val label: String,
    val value: String
) : Parcelable