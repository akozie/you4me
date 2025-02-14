package com.you4me.you4me.ui.main.messaging

import androidx.lifecycle.ViewModel

class ChatViewModel(
    val repository: ChatRepository,
) : ViewModel() {
//
//    private val _messages = MutableLiveData<List<Message>>()
//    val messages: LiveData<List<Message>> get() = _messages
//
//    fun sendMessage(
//        senderId: String,
//        receiverId: String,
//        message: String,
//    ) {
//        repository.sendMessage(senderId, receiverId, message) { success ->
//            if (success) {
//                // Handle success if needed
//            }
//        }
//    }
//
//    fun loadMessages() {
//        repository.getMessages { messagesList ->
//            _messages.postValue(messagesList)
//        }
//    }
}
