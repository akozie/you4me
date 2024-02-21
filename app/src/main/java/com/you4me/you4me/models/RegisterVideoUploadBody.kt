package com.you4me.you4me.models

data class RegisterVideoUploadBody(
    val fileURL: String,
    val userId: String,
    val videoId: String
)