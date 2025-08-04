package com.you4me.you4me.models.useroptions

import android.os.Parcelable
import com.you4me.you4me.models.ValueLabelResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserOptionsResponse(
    val ageGroups: List<AgeGroup>,
    val genders: List<Gender>,
    val religions: List<Religion>,
    val paymentModes: ArrayList<PaymentModes>,
    val sexualOrientations: List<SexualOrientation>,
    val countries: ArrayList<ValueLabelResponse>
) : Parcelable