package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.adapters.ChatListAdapter

data class Conversation(val providerName: String, val lastMessage: String, val time: String)

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Standardized Navigation
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        // Chat List
        val recyclerChatList = findViewById<RecyclerView>(R.id.recyclerChatList)
        recyclerChatList.layoutManager = LinearLayoutManager(this)
        
        val conversations = listOf(
            Conversation("Block 6 Provider", "Is the room still available?", "10:30 AM"),
            Conversation("Tlokweng Rentals", "Yes, you can visit tomorrow.", "Yesterday"),
            Conversation("G-West Apartments", "Deposit received, thank you!", "Mon")
        )

        recyclerChatList.adapter = ChatListAdapter(conversations) { conversation ->
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("partnerName", conversation.providerName)
            startActivity(intent)
        }
    }
}