package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.ChatMessageAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatMessageModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.showToast
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment(private val chatRoom: ChatRoomModel? = null) : Fragment() {

    private lateinit var chatMessageAdapter: ChatMessageAdapter
    private lateinit var collectionReference: CollectionReference
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
        } else {
            button_input.isEnabled = false
        }

        button_input.setOnClickListener {
            sendMessage()
        }
    }

    private fun readMessages() {
        /*
        collectionReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatMessages =
                        task.result?.documents?.map { ChatMessageModel(it.data!!) } as ArrayList
                    chatMessageAdapter = ChatMessageAdapter(chatMessages)
                    recycler_view_chat_messages.apply {
                        setHasFixedSize(true)
                        adapter = chatMessageAdapter
                        layoutManager = LayoutManagerWrapper(context, 1)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        (activity as MainActivity).showToast("메시지를 읽어오지 못했습니다.")
                        println("$TAG: ${task.exception}")
                    }
                }
            }
         */

        chatMessageAdapter = ChatMessageAdapter(mutableListOf())
        recycler_view_chat_messages.apply {
            setHasFixedSize(true)
            adapter = chatMessageAdapter
            layoutManager = LayoutManagerWrapper(context, 1)
        }

        listenerRegistration = collectionReference.addSnapshotListener { value, error ->
            if (error != null)
                println("$TAG: $error")
            else {
                for (change in value!!.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED ->
                            chatMessageAdapter.insert(ChatMessageModel(change.document.data))
                        DocumentChange.Type.MODIFIED ->
                            chatMessageAdapter.update(ChatMessageModel(change.document.data))
                        DocumentChange.Type.REMOVED ->
                            chatMessageAdapter.delete(ChatMessageModel(change.document.data))
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
        chatMessage.publicName = (activity as MainActivity).currentUserModel.publicName
        chatMessage.message = edit_text_message.text.toString()
        chatMessage.time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())

        collectionReference.add(chatMessage).addOnCompleteListener {  task ->
            if (task.isSuccessful)
                println("$TAG: Message sent")
            else {
                (activity as MainActivity).showToast("메시지 전송에 실패했습니다.")
                println("$TAG: ${task.exception}")
            }
        }
    }

    companion object {
        const val TAG = "ChatFragment"
    }
}
