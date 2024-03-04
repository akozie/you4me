package com.you4me.you4me.models

data class AddDateInterestBody(
    val proposedDate: String,
    val proposedTime: String,
    val submittedBy: String,
    val userId: String
)