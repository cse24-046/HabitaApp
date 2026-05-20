package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.adapters.ChatAdapter
import com.example.habita.database.AppDatabase
import com.example.habita.database.Message
import kotlinx.coroutines.launch

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private val messageList = mutableListOf<Message>()
    private lateinit var database: AppDatabase
    private var chatRoomId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_detail)

        database = AppDatabase.getDatabase(this)
        val partnerName = intent.getStringExtra("partnerName") ?: "Provider"
        findViewById<TextView>(R.id.txtChatPartnerName).text = partnerName
        
        // Generate a simple roomId: myId_providerName
        val myId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("userId", "user") ?: "user"
        chatRoomId = "${myId}_$partnerName"

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
        
        // Load existing messages for this room
        lifecycleScope.launch {
            database.messageDao().getMessagesForRoom(chatRoomId).collect { messages ->
                messageList.clear()
                messageList.addAll(messages)
                adapter.notifyDataSetChanged()
                if (messageList.isNotEmpty()) {
                    recyclerMessages.scrollToPosition(messageList.size - 1)
                }
            }
        }
        
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
                    senderId = "me",
                    chatRoomId = chatRoomId
                )
                
                lifecycleScope.launch {
                    database.messageDao().insertMessage(newMessage)
                    editMessage.text.clear()
                }
            }
        }
    }
}