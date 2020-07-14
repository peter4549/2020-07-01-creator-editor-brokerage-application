package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.ChatRoomAdapter
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_CHAT
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.KEY_CHAT_ROOM_MEMBER_IDS
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chat_rooms.*

class ChatRoomsFragment : Fragment() {

    private lateinit var chatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        readChatRooms()
    }

    private fun readChatRooms() {
        if (MainActivity.currentUser != null) {
            val currentUser = MainActivity.currentUser!!
                FirebaseFirestore.getInstance()
                    .collection(COLLECTION_CHAT)
                    .whereArrayContains(KEY_CHAT_ROOM_MEMBER_IDS, currentUser.uid)
                    .get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            chatRoomAdapter = ChatRoomAdapter(mutableListOf())

                            if (task.result != null)
                                if (task.result?.documents != null) {
                                    val chatRooms =
                                        task.result?.documents?.map { ChatRoomModel(it.data!!) }

                                    chatRoomAdapter =
                                        ChatRoomAdapter(chatRooms as MutableList<ChatRoomModel>)
                                }

                            recycler_view_chat_rooms.apply {
                                setHasFixedSize(true)
                                adapter = chatRoomAdapter
                                layoutManager = LayoutManagerWrapper(context, 1)
                            }
                        } else {
                            println("${TAG}: ${task.exception}")
                        }
                    }
        }
    }

    companion object {
        const val TAG ="ChatRoomFragment"
    }
}
