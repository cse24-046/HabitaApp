package com.example.habita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.activities.Conversation

class ChatListAdapter(
    private val conversations: List<Conversation>,
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtPartnerName: TextView = itemView.findViewById(R.id.txtPartnerName)
        val txtLastMessage: TextView = itemView.findViewById(R.id.txtLastMessage)
        val txtChatTime: TextView = itemView.findViewById(R.id.txtChatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = conversations[position]
        holder.txtPartnerName.text = chat.providerName
        holder.txtLastMessage.text = chat.lastMessage
        holder.txtChatTime.text = chat.time

        holder.itemView.setOnClickListener {
            onItemClick(chat)
        }
    }

    override fun getItemCount(): Int = conversations.size
}