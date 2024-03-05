package com.you4me.you4me.models

data class ProposeNewDateTimeBody(
    val dateId: String,
    val interestId: String,
    val proposedDate: String,
    val proposedTime: String
)