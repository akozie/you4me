package com.you4me.you4me.models

data class InviteeDatesRequiringApprovalItem(
    val category: String,
    val date: String,
    val dateId: String,
    val interestId: String,
    val name: String,
    val place: String,
    val rawDate: String,
    val time: String,
    val userId: String
)