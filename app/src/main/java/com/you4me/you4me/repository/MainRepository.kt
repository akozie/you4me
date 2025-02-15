package com.you4me.you4me.repository

import android.util.Log
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.you4me.you4me.models.AddDateInterestBody
import com.you4me.you4me.models.AddSwipeBody
import com.you4me.you4me.models.ProposeNewDateTimeBody
import com.you4me.you4me.models.RejectDateInterestBody
import com.you4me.you4me.models.SubmitDateBody
import com.you4me.you4me.models.UpdateDateInterestBody
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.ui.main.messaging.model.Message

class MainRepository(private val apiCollector: ApiCollector) : BaseRepository() {
    suspend fun updatePushToken(
        userId: String,
        token: JsonObject,
    ) = safeApiCall { apiCollector.updatePushToken(userId, token) }

    suspend fun getNotifications(userId: String) = safeApiCall { apiCollector.getNotifications(userId) }

    suspend fun markNotificationAsRead(notificationId: String) = safeApiCall { apiCollector.markNotificationAsRead(notificationId) }

    suspend fun getPaymentModes() = safeApiCall { apiCollector.getPaymentModes() }

    suspend fun submitDate(submitDateBody: SubmitDateBody) = safeApiCall { apiCollector.submitDate(submitDateBody) }

    suspend fun fetchDates(userId: String) =
        safeApiCall {
            apiCollector.fetchDates(userId)
        }

    suspend fun addDateInterest(
        dateId: String,
        addDateInterestBody: AddDateInterestBody,
    ) = safeApiCall {
        apiCollector.addDateInterest(dateId, addDateInterestBody)
    }

    suspend fun addSwipe(addSwipeBody: AddSwipeBody) =
        safeApiCall {
            apiCollector.addSwipe(addSwipeBody)
        }

    suspend fun sendPushNotification(notification: JsonObject) =
        safeApiCall {
            apiCollector.sendPushNotification(notification)
        }

    suspend fun fetchDateInterest(userId: String) =
        safeApiCall {
            apiCollector.fetchDateInterests(userId)
        }

    suspend fun getSubscriptionStatus(userId: String) =
        safeApiCall {
            apiCollector.getSubscriptionStatus(userId)
        }

    suspend fun getExistingUser(userId: String) = safeApiCall { apiCollector.getUser(userId) }

    suspend fun rejectDateInterest(
        interestId: String,
        rejectDate: RejectDateInterestBody,
    ) = safeApiCall {
        apiCollector.rejectDateInterest(interestId, rejectDate)
    }

    suspend fun acceptDateInterest(
        interestId: String,
        acceptDate: UpdateDateInterestBody,
    ) = safeApiCall { apiCollector.updateDateInterest(interestId, acceptDate) }

    suspend fun getUpcomingDates(userId: String) = safeApiCall { apiCollector.getUpcomingDates(userId) }

    suspend fun inviteeDatesRequiringApproval(userId: String) = safeApiCall { apiCollector.getInviteeDatesRequiringApproval(userId) }

    suspend fun getDateInterestsRequiringApproval(userId: String) =
        safeApiCall {
            apiCollector.getDateInterestsRequiringApproval(userId)
        }

    suspend fun fetchCompletedDates(userId: String) =
        safeApiCall {
            apiCollector.fetchCompletedDates(userId)
        }

    suspend fun proposeNewDateTime(
        userId: String,
        interestId: String,
        body: ProposeNewDateTimeBody,
    ) = safeApiCall {
        apiCollector.proposeNewDateTime(userId, interestId, body)
    }

    suspend fun registerPayment(obj: JsonObject) = safeApiCall { apiCollector.registerPayment(obj) }

    suspend fun updateReview(obj: JsonObject) = safeApiCall { apiCollector.updateReview(obj) }

    suspend fun getImageVideoUpload(userId: String) =
        safeApiCall {
            apiCollector.getImageVideoUpload(userId)
        }

    private val dbRef = FirebaseDatabase.getInstance().getReference("chats")

    private fun getChatId(
        senderId: String,
        receiverId: String,
    ): String {
        return if (senderId < receiverId) "$senderId-$receiverId" else "$receiverId-$senderId"
    }

    fun sendMessage(
        senderId: String,
        receiverId: String,
        text: String,
        callback: (Boolean) -> Unit,
    ) {
        val chatId = getChatId(senderId, receiverId)
        val messageId = dbRef.child(chatId).child("messages").push().key ?: return

//        val currentDateTime = LocalDateTime.now() // Get current date and time
//        val formattedDateTime = currentDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val message =
            Message(
                messageId = messageId,
                senderId = senderId,
                senderName = "Sender Name", // Update dynamically
                senderImage = "Sender Image", // Update dynamically
                recipientId = receiverId,
                recipientName = "Receiver Name", // Update dynamically
                recipientImage = "Receiver Image", // Update dynamically
                text = text,
                timestamp = System.currentTimeMillis(),
                seen = false,
            )

        dbRef.child(chatId).child("messages").child(messageId).setValue(message)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

//    fun markMessagesAsSeen(
//        chatId: String,
//        senderId: String,
//        callback: () -> Unit,
//    ) {
//        // Reference to the messages node under the specific chatId
//        val messagesRef = dbRef.child(chatId).child("messages")
//        Log.d("OK_SEEN_REPOSI", "Checking message: $messagesRef")
//
//        messagesRef.addListenerForSingleValueEvent(
//            object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    var messageUpdated = false
//
//                    // Loop through all the messages in the chat
//                    for (child in snapshot.children) {
//                        val messageData = child.value as? Map<String, Any>
//                        val messageSenderId = messageData?.get("senderId") as? String
//                        val seen = messageData?.get("seen") as? Boolean ?: false
//                        Log.d("OK_SEEN_REPOSITTTT", "Checking messageeeee: ${child.key}")
//                        Log.d("OK_SEEN_REPOSITTTTWE", "Check$messageSenderId===$senderId")
//
//                        // Check if the message is unseen and sent by the other user
//                        if (!seen && messageSenderId != senderId) {
//                            // Update the 'seen' field for the specific message
//                            val updates = mapOf("seen" to true)
//                            child.ref.updateChildren(updates).addOnCompleteListener { task ->
//                                if (task.isSuccessful) {
//                                    messageUpdated = true
//                                    Log.d("OK_SEEN_UPDATED", "Message marked as seen: ${child.key}")
//                                } else {
//                                    Log.e("OK_SEEN_UPDATE_FAILED", "Failed to mark message as seen: ${child.key}")
//                                }
//                            }
//                        }
//                    }
//
//                    // If any messages were updated, trigger the callback
//                    if (messageUpdated) {
//                        callback() // Trigger callback after the update
//                    } else {
//                        Log.d("OK_SEEN_NO_MESSAGES", "No messages to mark as seen.")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("FirebaseError", "Error updating messages: ${error.message}")
//                    Log.d("OK_SEEN_FAILED", "Failed to update seen status.")
//                }
//            },
//        )
//    }

    fun markMessagesAsSeen(
        chatId: String,
        receiverId: String,
        senderId: String,
        callback: () -> Unit,
    ) {
        val messagesRef = dbRef.child(chatId).child("messages")

        messagesRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { messageSnapshot ->
                        val message = messageSnapshot.getValue(Message::class.java)

                        if (message != null) {
                            // Check if the message is not sent by the sender and has not been marked as seen
                            if (message.recipientId != senderId && !message.seen) {
                                // Update the 'seen' field for the specific message
                                messageSnapshot.ref.child("seen").setValue(true)

                                // Optionally log the update
                                Log.d("Firebase_OKOK", "Message marked as seen: $message")
                            }
                        } else {
                            Log.e("Firebase", "Invalid message format: ${messageSnapshot.value}")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error reading messages", error.toException())
                }
            },
        )
    }

    fun getMessages(
        chatId: String,
        callback: (List<Message>) -> Unit,
    ) {
        dbRef.child("chats").child(chatId)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val messagesList = mutableListOf<Message>()
                        for (child in snapshot.children) {
                            val messageData = child.value as? Map<String, Any>
                            val message =
                                messageData?.let {
                                    Message(
                                        messageId = child.key.orEmpty(),
                                        senderId = it["senderId"] as? String ?: "",
                                        senderName = it["senderName"] as? String ?: "",
                                        senderImage = it["senderImage"] as? String ?: "",
                                        recipientId = it["recipientId"] as? String ?: "",
                                        recipientName = it["recipientName"] as? String ?: "",
                                        recipientImage = it["recipientImage"] as? String ?: "",
                                        text = it["text"] as? String ?: "",
                                        timestamp = it["timestamp"] as? Long ?: 0L,
                                        seen = it["seen"] as? Boolean ?: false,
                                    )
                                }
                            message?.let { messagesList.add(it) }
                        }
                        callback(messagesList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle any errors
                    }
                },
            )
    }

    fun listenForNewMessages(
        senderId: String,
        receiverId: String,
        callback: (Message) -> Unit,
    ) {
        val chatId = getChatId(senderId, receiverId)

        dbRef.child(chatId).child("messages").orderByChild("timestamp")
            .addChildEventListener(
                object : ChildEventListener {
                    override fun onChildAdded(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {
                        val message = snapshot.getValue(Message::class.java)
                        message?.let { callback(it) }
                    }

                    override fun onChildChanged(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {}

                    override fun onChildRemoved(snapshot: DataSnapshot) {}

                    override fun onChildMoved(
                        snapshot: DataSnapshot,
                        previousChildName: String?,
                    ) {}

                    override fun onCancelled(error: DatabaseError) {}
                },
            )
    }

    fun setTypingStatus(
        isTyping: Boolean,
        senderId: String,
        recipientId: String,
        chatId: String,
    ) {
        val typingRef = dbRef.child("typing_status").child(chatId).child(senderId)
        val typingData =
            mapOf(
                "recipientId" to recipientId,
                "isTyping" to isTyping,
            )

        typingRef.setValue(typingData)
    }

    fun observeTypingStatus(
        senderId: String,
        recipientId: String,
        chatId: String,
        callback: (Boolean) -> Unit,
    ) {
        val typingRef = dbRef.child("typing_status").child(chatId)

        typingRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val data = child.value as? Map<String, Any> ?: continue
                        val isTyping = data["isTyping"] as? Boolean ?: false
                        val targetId = data["recipientId"] as? String ?: ""

                        if (child.key != senderId && targetId == senderId) { // Other user is typing
                            callback(isTyping)
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            },
        )
    }
}
