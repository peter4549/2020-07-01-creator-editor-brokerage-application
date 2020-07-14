package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_CHAT
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_CHAT_MESSAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatMessageModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_pr.view.text_view_public_name
import kotlinx.android.synthetic.main.item_view_message.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment(private val chatRoom: ChatRoomModel? = null) : Fragment() {

    private lateinit var chatMessageRecyclerViewAdapter: ChatMessageRecyclerViewAdapter
    private lateinit var collectionReference: CollectionReference
    private val publicName = MainActivity.currentUserDataModel?.publicName
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (chatRoom != null) {
            button_input.isEnabled = true
            collectionReference = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHAT).document(chatRoom.roomId)
                .collection(COLLECTION_CHAT_MESSAGES)
            readMessages()
        } else
            button_input.isEnabled = false

        button_input.setOnClickListener {
            sendMessage()
        }
    }

    private fun readMessages() {
        chatMessageRecyclerViewAdapter = ChatMessageRecyclerViewAdapter(mutableListOf())
        recycler_view_chat_messages.apply {
            setHasFixedSize(true)
            adapter = chatMessageRecyclerViewAdapter
            layoutManager = LayoutManagerWrapper(context, 1)
        }

        listenerRegistration = collectionReference.addSnapshotListener { value, error ->
            if (error != null)
                println("$TAG: $error")
            else {
                for (change in value!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED ->
                            chatMessageRecyclerViewAdapter.insert(ChatMessageModel(change.document.data))
                        DocumentChange.Type.MODIFIED ->
                            chatMessageRecyclerViewAdapter.update(ChatMessageModel(change.document.data))
                        DocumentChange.Type.REMOVED ->
                            chatMessageRecyclerViewAdapter.delete(ChatMessageModel(change.document.data))
                        else -> {  }
                    }
                }
            }
        }
    }

    private fun sendMessage() {
        if (edit_text_message.text.isBlank())
            return

        val chatMessage = ChatMessageModel()
        chatMessage.publicName = publicName
        chatMessage.message = edit_text_message.text.toString()
        chatMessage.time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

        collectionReference.add(chatMessage).addOnCompleteListener {  task ->
            if (task.isSuccessful)
                println("$TAG: Message sent")
            else {
                showToast(requireContext(), "메시지 전송에 실패했습니다.")
                println("$TAG: ${task.exception}")
            }
        }
    }

    class ChatMessageRecyclerViewAdapter(private val chatMessages: MutableList<ChatMessageModel>) :
        RecyclerView.Adapter<ChatMessageRecyclerViewAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ChatMessageRecyclerViewAdapter.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_message, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return chatMessages.count()
        }

        override fun onBindViewHolder(holder: ChatMessageRecyclerViewAdapter.ViewHolder, position: Int) {
            val message = chatMessages[position]
            holder.view.text_view_public_name.text = message.publicName
            holder.view.text_view_message.text = message.message
        }

        private fun getPosition(chatMessage: ChatMessageModel) = chatMessages.indexOf(chatMessage)

        fun insert(chatMessage: ChatMessageModel) {
            chatMessages.add(chatMessages.size, chatMessage)
            notifyItemInserted(chatMessages.size)
        }

        fun update(chatMessage: ChatMessageModel) {
            notifyItemChanged(getPosition(chatMessage))
        }

        fun delete(chatMessage: ChatMessageModel) {
            chatMessages.remove(chatMessage)
            notifyItemRemoved(getPosition(chatMessage))
        }
    }

    companion object {
        const val TAG = "ChatFragment"
    }
}
