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

// 인증해야만 쓸 수 있는 서비로로, 탭 레벨에서 제한할 것.
class ChatRoomsFragment : Fragment() {

    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        recycler_view_chat_rooms.apply {
            setHasFixedSize(true)
            adapter = ChatRoomRecyclerViewAdapter()
            layoutManager = LayoutManagerWrapper(context, 1)
        }
    }

    private fun enterChatRoom(chatRoom: ChatRoomModel) {
        (activity as MainActivity)
            .startFragment(ChatFragment(chatRoom), R.id.frame_layout_chat_rooms, CHAT_FRAGMENT_TAG)
    }

    inner class ChatRoomRecyclerViewAdapter :
        RecyclerView.Adapter<ChatRoomRecyclerViewAdapter.ViewHolder>() {

        private val chatRooms = mutableListOf<ChatRoomModel>()

        inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        init {
            setChatRoomSnapshotListener(this)
        }

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
            holder.view.text_view_message.text = chatRoom.lastMessage
            holder.view.setOnClickListener {
                enterChatRoom(chatRoom)
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

    private fun setChatRoomSnapshotListener(chatRoomRecyclerViewAdapter: ChatRoomRecyclerViewAdapter) {
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

    fun removeChatRoomSnapshotListener() {
        if(::listenerRegistration.isInitialized)
            listenerRegistration.remove()
    }

    companion object {
        const val TAG ="ChatRoomFragment"
    }
}
