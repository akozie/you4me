package com.you4me.you4me.models

data class SubmitDateBody(
    val date: String,
    val paymentMode: String,
    val place: Place,
    val time: String,
    val userId: String,
    val type: String
)