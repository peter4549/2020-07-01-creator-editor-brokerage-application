package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatRoomModel
import kotlinx.android.synthetic.main.item_view_chat_room.view.*

class ChatRoomAdapter(private val chatRooms: MutableList<ChatRoomModel>): RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {

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
        holder.view.text_view_public_name.text = chatRoom.memberPublicNames.joinToString()
        holder.view.text_view_message.text = chatRoom.latestMessage
    }
}