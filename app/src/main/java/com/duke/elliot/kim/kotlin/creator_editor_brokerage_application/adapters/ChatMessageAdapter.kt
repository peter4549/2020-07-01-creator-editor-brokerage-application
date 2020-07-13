package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.ChatMessageModel
import kotlinx.android.synthetic.main.fragment_pr.view.text_view_public_name
import kotlinx.android.synthetic.main.item_view_message.view.*

class ChatMessageAdapter(private val chatMessages: MutableList<ChatMessageModel>) : RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatMessageAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_message, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatMessages.count()
    }

    override fun onBindViewHolder(holder: ChatMessageAdapter.ViewHolder, position: Int) {
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