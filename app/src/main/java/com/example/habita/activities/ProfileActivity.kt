package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.habita.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Standardized Navigation Logic
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

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnEditPreferences = findViewById<TextView>(R.id.btnEditProfile)
        val btnNotifications = findViewById<TextView>(R.id.btnNotifications)
        val btnSupport = findViewById<TextView>(R.id.btnSupport)

        // Now opens the Preferences Activity
        btnEditPreferences.setOnClickListener {
            startActivity(Intent(this, PreferencesActivity::class.java))
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications are active for matches!", Toast.LENGTH_SHORT).show()
        }

        btnSupport.setOnClickListener {
            Toast.makeText(this, "Support center is under maintenance", Toast.LENGTH_SHORT).show()
        }
    }
}