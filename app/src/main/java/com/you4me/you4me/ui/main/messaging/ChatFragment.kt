package com.you4me.you4me.ui.main.messaging

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.you4me.you4me.R
import com.you4me.you4me.databinding.FragmentChatBinding
import com.you4me.you4me.models.User
import com.you4me.you4me.network.ApiCollector
import com.you4me.you4me.network.Resource
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
    private lateinit var receiverName: String
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
        receiverName = userId.recipientName
        chatAdapter = ChatAdapter(messages, user.userId)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Send message using the viewModel
                viewModel.sendMessage(senderID, receiverId, messageText, senderName, chatId, receiverName)

                // Clear the input field
                messageInput.text.clear()
            }
        }

        binding.reportDate.setOnClickListener {
            showReportAbuseDialog(userId.dateId, userId.senderId)
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
            // Create a mutable list of current messages
            val currentMessages = messages.toMutableList()

            newMessages.forEach { newMessage ->
                // Check if the message is already in the list
                val isMessageAlreadyAdded = currentMessages.any { it.messageId == newMessage.messageId }
                if (!isMessageAlreadyAdded) {
                    messages.add(newMessage)
                }
            }

            // Sort messages by timestamp (assuming `timestamp` is a Long)
            messages.sortBy { it.timestamp }

            // Notify adapter of the change and scroll to the last message
            chatAdapter.notifyDataSetChanged()
            recyclerView.scrollToPosition(messages.size - 1)
        }

        // Mark messages as seen when the fragment is resumed
        markMessagesAsSeen()
        viewModel.loadMessages(chatId)
        // Start listening for typing status
        viewModel.listenForTyping(senderID, receiverId, chatId)

        // Example: Set typing status when user starts typing
        binding.editTextMessage.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    viewModel.setTypingStatus(!s.isNullOrEmpty(), senderID, receiverId, chatId)
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int,
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int,
                ) {}
            },
        )

        viewModel.isRecipientTyping.observe(viewLifecycleOwner) { isTyping ->
            if (isTyping) {
                showTypingIndicator()
            } else {
                hideTypingIndicator()
            }
        }

//        viewModel.listenForNewMessages(senderID, receiverId)
    }

    private fun showReportAbuseDialog(
        dateId: String,
        userId: String,
    ) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_report_abuse, null)

        // Initialize UI elements
        val etFeedback = dialogView.findViewById<EditText>(R.id.et_feedback)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val policy = dialogView.findViewById<TextView>(R.id.see_policy_link)

        // Create AlertDialog
        val dialog =
            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create()

        // Handle submit button click
        btnSubmit.setOnClickListener {
            showLoading(true)
            val feedback = etFeedback.text.toString().trim()
            val obj =
                JsonObject().apply {
                    addProperty("date_id", dateId)
                    addProperty("user_id", userId)
                    addProperty("thumbs_up", false)
                    addProperty("comment", feedback)
                }
            viewModel.updateReview(obj)
            viewModel.updateReviewResponse.observe(viewLifecycleOwner) {
                showLoading(false)
                when (it) {
                    is Resource.Success -> {
                        mixpanel?.track("Android_Chat_Report_Date_Button_Clicked")
                        findNavController().popBackStack()
                    }

                    is Resource.Failure -> {
                    }
                }
            }

            dialog.dismiss() // Close the dialog
        }

        btnCancel.setOnClickListener {
            showLoading(false)
            dialog.dismiss()
        }
        policy.setOnClickListener {
            policy.text =
                HtmlCompat.fromHtml(
                    getString(R.string.we_frown_against_child_abuse_see_policy_here_play_your_part_in_reporting_a_suspected_child_abuse),
                    HtmlCompat.FROM_HTML_MODE_LEGACY,
                )
            policy.movementMethod = LinkMovementMethod.getInstance() // Makes the link clickable
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.you4me.social/child-abuse-policy"))
            it.context.startActivity(intent)
        }
        // Show the dialog
        dialog.show()
    }

    private fun showLoading(loading: Boolean) {
        binding.loader.visibility = if (loading) View.VISIBLE else View.GONE
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

    private fun showTypingIndicator() {
        binding.typingIndicator.visibility = View.VISIBLE
        binding.typingIndicator.text = "${user.name} is Typing"
    }

    private fun hideTypingIndicator() {
        binding.typingIndicator.visibility = View.GONE
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
        mixpanel?.flush()
        mixpanel?.optOutTracking()
    }
}
