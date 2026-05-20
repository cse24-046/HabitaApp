package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.adapters.ChatAdapter
import com.example.habita.database.Message

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        val partnerName = intent.getStringExtra("partnerName") ?: "Provider"
        findViewById<TextView>(R.id.txtChatPartnerName).text = partnerName

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val recyclerMessages = findViewById<RecyclerView>(R.id.recyclerMessages)
        val editMessage = findViewById<EditText>(R.id.editMessage)
        val btnSendMessage = findViewById<ImageButton>(R.id.btnSendMessage)

        adapter = ChatAdapter(messageList)
        recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerMessages.adapter = adapter
        
        // Navigation Icons
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        btnSendMessage.setOnClickListener {
            val text = editMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val newMessage = Message(
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    senderId = "me" // Dummy current user ID
                )
                adapter.addMessage(newMessage)
                recyclerMessages.smoothScrollToPosition(adapter.itemCount - 1)
                editMessage.text.clear()
            }
        }
    }
}