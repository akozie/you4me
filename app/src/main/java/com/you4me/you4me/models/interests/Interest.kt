package com.you4me.you4me.models.interests

data class Interest(
    val age: String,
    val complement: String,
    val createdAt: String,
    val dateID: String,
    val interestID: String,
    val name: String,
    val originalDate: String,
    val originalTime: String,
    val proposedDate: String,
    val proposedTime: String,
    val requestedVerificationByReceiver: Boolean,
    val requestedVerificationBySender: Boolean,
    val state: String,
    val status: String,
    val submittedBy: String,
    val userID: String,
    val venue: String,
    val videoURL: String
)