package com.example.habita.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var txtProfileName: TextView
    private lateinit var txtProfileEmail: TextView
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        database = AppDatabase.getDatabase(this)
        txtProfileName = findViewById(R.id.profileName)
        txtProfileEmail = findViewById(R.id.profileEmail)
        
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val role = sharedPref.getString("userRole", "student")

        loadUserProfile()

        // Standardized Navigation Logic
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            if (role == "provider") {
                startActivity(Intent(this, ProviderHomeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
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
        val btnEditDetails = findViewById<TextView>(R.id.btnEditProfile)
        val btnNotifications = findViewById<TextView>(R.id.btnNotifications)
        val btnSupport = findViewById<TextView>(R.id.btnSupport)

        // Remove Preferences from profile - changed to just edit details
        btnEditDetails.setOnClickListener {
            Toast.makeText(this, "Edit details coming soon", Toast.LENGTH_SHORT).show()
        }

        // Providers shouldn't see notifications/support in the same way? 
        // User said: "remove preferences from profile editing... what are they doing with preferences?"
        // I will keep the layout simple and matching as requested.

        btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications active", Toast.LENGTH_SHORT).show()
        }

        btnSupport.setOnClickListener {
            Toast.makeText(this, "Support center is under maintenance", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("userId", null) ?: return

        lifecycleScope.launch {
            val user = database.userDao().getUserById(userId)
            if (user != null) {
                txtProfileName.text = user.name
                txtProfileEmail.text = user.email
            }
        }
    }
}