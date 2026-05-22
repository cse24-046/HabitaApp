package com.example.habita.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.adapters.ChatListAdapter
import com.example.habita.database.AppDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Conversation(val providerName: String, val lastMessage: String, val time: String)

class ChatActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        database = AppDatabase.getDatabase(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "student")

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Hide bottom nav if provider
        val layoutBottomNav = findViewById<LinearLayout>(R.id.layoutBottomNav)
        if (role == "provider") {
            layoutBottomNav.visibility = View.GONE
        }

        // Student bottom navigation listeners
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

        val recyclerChatList = findViewById<RecyclerView>(R.id.recyclerChatList)
        recyclerChatList.layoutManager = LinearLayoutManager(this)
        
        // Load only real conversations from local Room Database
        lifecycleScope.launch {
            database.messageDao().getLatestMessagesPerRoom().collect { messages ->
                val conversations = messages.map { msg ->
                    Conversation(
                        providerName = msg.chatRoomId.substringAfter("_"),
                        lastMessage = msg.text,
                        time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                    )
                }

                if (conversations.isEmpty()) {
                    Toast.makeText(this@ChatActivity, "No active chats.", Toast.LENGTH_LONG).show()
                }

                recyclerChatList.adapter = ChatListAdapter(conversations) { conversation ->
                    val intent = Intent(this@ChatActivity, ChatDetailActivity::class.java)
                    intent.putExtra("partnerName", conversation.providerName)
                    startActivity(intent)
                }
            }
        }
    }
}