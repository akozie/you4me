package com.you4me.you4me.models

data class RegisterProfilePhotoBody(
    val fileURL: String,
    val userId: String,
    val videoId: String,
    val category: String,
    val isProfilePhoto: Boolean,
)


data class RegisterVideoUploadBody(
    val fileURL: String,
    val userId: String,
    val videoId: String,
    val category: String,
)
