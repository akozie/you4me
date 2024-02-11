package com.you4me.you4me.models

data class LoginResponse(
    val agePreferred: String,
    val convertedDate: Int,
    val country: String,
    val dob: String,
    val email: String,
    val gender: String,
    val isVideoBeingReviewed: String,
    val name: String,
    val password: String,
    val phone: String,
    val religion: String,
    val religionPreferred: String,
    val sexualOrientation: String,
    val state: String,
    val status: String,
    val token: String,
    val userId: String,
    val videoStatus: String,
    val videoURL: String
)