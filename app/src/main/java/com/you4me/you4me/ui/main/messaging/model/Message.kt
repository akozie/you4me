package com.you4me.you4me.ui.main.messaging.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImage: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val recipientImage: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(), // TimeInterval in Swift = Long in Kotlin (milliseconds)
    val seen: Boolean = false,
)
