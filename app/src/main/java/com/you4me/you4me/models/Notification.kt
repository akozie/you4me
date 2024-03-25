package com.you4me.you4me.models

data class Notification(
    val message: String,
    val notify_id: String,
    val seen: String,
    val user_id: String,
    val date: String,
    val time: String,
)