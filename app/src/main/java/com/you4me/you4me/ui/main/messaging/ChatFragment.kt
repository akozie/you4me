package com.you4me.you4me.ui.main.messaging

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.gson.Gson
import com.you4me.you4me.databinding.FragmentChatBinding
import com.you4me.you4me.models.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.repository.MainRepository
import com.you4me.you4me.ui.base.BaseFragment
import com.you4me.you4me.ui.main.MainViewModel
import com.you4me.you4me.ui.main.messaging.model.Message
import com.you4me.you4me.utils.SharedPrefHelper

class ChatFragment : BaseFragment<MainViewModel, FragmentChatBinding, MainRepository>("CHAT_SCREEN") {
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var senderID: String
    private lateinit var receiverId: String
    private lateinit var chatId: String
    private lateinit var senderName: String
    private lateinit var user: User
    private lateinit var messageListener: ChildEventListener

    private val messages = mutableListOf<Message>()
    private lateinit var messagesRef: DatabaseReference

    private var isFragmentVisible = false

    private val args: ChatFragmentArgs by navArgs()

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentChatBinding {
        return FragmentChatBinding.inflate(inflater, container, false)
    }

    override fun getRepository() = MainRepository(dataSource.buildApi(ApiCollector::class.java))

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val userProfile = sharedPrefHelper.getString(SharedPrefHelper.USER_PROFILE)
        val gson = Gson()
        user = gson.fromJson(userProfile, User::class.java)
        val userId = args.CHAT
        Log.d("ChatFragment", "Received userId: $userId")

        // Initialize the chatId
        chatId = userId.dateId
//        chatId = "06f9cc96-a01e-4238-a2a1-588172059494-fffab5c1-1c84-4e1a-ba66-bfbdff033f06"

        recyclerView = binding.recyclerViewChat
        messageInput = binding.editTextMessage
        sendButton = binding.buttonSend

//        receiverId = "fffab5c1-1c84-4e1a-ba66-bfbdff033f06"
//        senderID = "06f9cc96-a01e-4238-a2a1-588172059494" // Replace with actual receiver ID

        // doogee
        senderID = userId.senderId
        receiverId = userId.recipientId

//        // doogee
//        senderID = "fffab5c1-1c84-4e1a-ba66-bfbdff033f06"
//        receiverId = "06f9cc96-a01e-4238-a2a1-588172059494" // Replace with actual receiver ID
        senderName = userId.senderName
        chatAdapter = ChatAdapter(messages, user.userId)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Send message using the viewModel
                viewModel.sendMessage(senderID, receiverId, messageText, senderName, chatId)

                // Clear the input field
                messageInput.text.clear()
            }
        }

        // Set up Firebase reference for messages
        messagesRef = FirebaseDatabase.getInstance().getReference("chats/$chatId")

        // Add child event listener
        messageListener =
            object : ChildEventListener {
                override fun onChildAdded(
                    snapshot: DataSnapshot,
                    previousChildName: String?,
                ) {
                    val message = snapshot.getValue(Message::class.java)
                    message?.let { processMessage(it) }
                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?,
                ) {
                    val updatedMessage = snapshot.getValue(Message::class.java)
                    updatedMessage?.let {
                        if (isFragmentVisible) {
                            // If the message is updated, reflect changes
                            val position = messages.indexOfFirst { it.messageId == updatedMessage.messageId }
                            if (position != -1) {
                                messages[position] = updatedMessage
                                chatAdapter.notifyItemChanged(position)
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle message removed
                }

                override fun onChildMoved(
                    snapshot: DataSnapshot,
                    previousChildName: String?,
                ) {
                    // Handle message moved
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error in child event listener", error.toException())
                }
            }

        messagesRef.addChildEventListener(messageListener)

        // Observe messages from ViewModel
        viewModel.messages.observe(viewLifecycleOwner) { newMessages ->
            // Clear the old messages and add only the new messages
            val currentMessages = messages.toMutableList()
            newMessages.forEach { newMessage ->
                val isMessageAlreadyAdded = currentMessages.any { it.messageId == newMessage.messageId }
                if (!isMessageAlreadyAdded) {
                    messages.add(newMessage)
                }
            }
            chatAdapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(messages.size - 1)
        }

        // Mark messages as seen when the fragment is resumed
        markMessagesAsSeen()
    }

    private fun processMessage(message: Message) {
        if (isFragmentVisible) {
            // Only update the messages if the fragment is visible
            val isMessageAlreadyAdded = messages.any { it.messageId == message.messageId }
            if (!isMessageAlreadyAdded) {
                messages.add(message)
                chatAdapter.notifyItemInserted(messages.size - 1)
                recyclerView.scrollToPosition(messages.size - 1)

                // Mark as seen if the message is for the current user and not seen yet
                if (message.senderId != user.userId && !message.seen) {
                    markMessageAsSeen(message.messageId)
                }
            }
        }
    }

    private fun markMessageAsSeen(messageId: String) {
        // Mark the message as seen in Firebase
        val messageRef = messagesRef.child(messageId)
        messageRef.child("seen").setValue(true)
    }

    private fun markMessagesAsSeen() {
        // Replace with actual chatId and senderId logic
        viewModel.markMessagesAsSeen(chatId = chatId, senderId = senderID, receiverId)
        Log.d("OK_SEEN", "OK_SEEN")
    }

    override fun onResume() {
        super.onResume()
        // Set the flag to true when the fragment is visible
        isFragmentVisible = true
    }

    override fun onPause() {
        super.onPause()
        // Set the flag to false when the fragment is paused
        isFragmentVisible = false
        // Remove the listener when the fragment is paused to prevent updates while not visible
        messagesRef.removeEventListener(messageListener)
    }

    override fun onStop() {
        super.onStop()
        // Set the flag to false when the fragment is stopped
        isFragmentVisible = false
        // Remove the listener when the fragment is stopped to prevent updates while not visible
        messagesRef.removeEventListener(messageListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFragmentVisible = false
        // Clean up and remove the listener when the fragment view is destroyed
        messagesRef.removeEventListener(messageListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        isFragmentVisible = false
        // Clean up and remove the listener when the fragment view is destroyed
        messagesRef.removeEventListener(messageListener)
    }
}
