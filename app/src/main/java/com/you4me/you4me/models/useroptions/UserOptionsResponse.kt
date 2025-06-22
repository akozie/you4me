package com.you4me.you4me.models.useroptions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserOptionsResponse(
    val ageGroups: List<AgeGroup>,
    val genders: List<Gender>,
    val religions: List<Religion>,
    val sexualOrientations: List<SexualOrientation>
) : Parcelable