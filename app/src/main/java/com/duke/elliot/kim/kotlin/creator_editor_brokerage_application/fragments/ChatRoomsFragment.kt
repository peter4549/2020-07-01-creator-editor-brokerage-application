package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity.Companion.CHAT_FRAGMENT_TAG
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.constants.COLLECTION_CHAT
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.fragment_chat_rooms.*
import kotlinx.android.synthetic.main.item_view_chat_room.view.*

class ChatRoomsFragment : Fragment() {

    private lateinit var chatRoomRecyclerViewAdapter: ChatRoomRecyclerViewAdapter
    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChatRoomListener()
    }

    private fun setChatRoomListener() {
        chatRoomRecyclerViewAdapter = ChatRoomRecyclerViewAdapter(mutableListOf())
        recycler_view_chat_rooms.apply {
            setHasFixedSize(true)
            adapter = chatRoomRecyclerViewAdapter
            layoutManager = LayoutManagerWrapper(context, 1)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            listenerRegistration = FirebaseFirestore.getInstance()
                .collection(COLLECTION_CHAT)
                .whereArrayContains(ChatRoomModel.KEY_USER_IDS,
                    FirebaseAuth.getInstance().currentUser?.uid.toString())
                .addSnapshotListener { value, error ->
                    if (error != null)
                        println("$TAG: $error")
                    else {
                        for (change in value!!.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED ->
                                    chatRoomRecyclerViewAdapter.insert(ChatRoomModel(change.document.data))
                                DocumentChange.Type.MODIFIED ->
                                    chatRoomRecyclerViewAdapter.update(ChatRoomModel(change.document.data))
                                DocumentChange.Type.REMOVED ->
                                    chatRoomRecyclerViewAdapter.delete(ChatRoomModel(change.document.data))
                                else -> {  }
                            }
                        }
                    }
                }
        }
    }

    private fun enterExistingChatRoom(chatRoom: ChatRoomModel) {
        (activity as MainActivity)
            .startFragment(ChatFragment(chatRoom), R.id.frame_layout_chat_rooms, CHAT_FRAGMENT_TAG)
    }

    inner class ChatRoomRecyclerViewAdapter(private val chatRooms: MutableList<ChatRoomModel>):
        RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ViewHolder>() {

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_chat_room, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return chatRooms.count()
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val chatRoom = chatRooms[position]
            holder.view.text_view_public_name.text = chatRoom.userPublicNames.joinToString()
            holder.view.text_view_message.text = chatRoom.latestMessage
            holder.view.setOnClickListener {
                enterExistingChatRoom(chatRoom)
            }
        }

        fun insert(chatRoom: ChatRoomModel) {
            chatRooms.add(chatRooms.size, chatRoom)
            notifyItemInserted(chatRooms.size)
        }

        fun update(chatRoom: ChatRoomModel) {
            notifyItemChanged(getPosition(chatRoom))
        }

        fun delete(chatRoom: ChatRoomModel) {
            chatRooms.remove(chatRoom)
            notifyItemRemoved(getPosition(chatRoom))
        }

        private fun getPosition(chatRoom: ChatRoomModel) = chatRooms.indexOf(chatRoom)
    }

    companion object {
        const val TAG ="ChatRoomFragment"
    }
}
