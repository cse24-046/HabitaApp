package com.example.habita.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        database = AppDatabase.getDatabase(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("userId", "") ?: ""

        val etName = findViewById<EditText>(R.id.etEditName)
        val etEmail = findViewById<EditText>(R.id.etEditEmail)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        lifecycleScope.launch {
            val user = database.userDao().getUserById(userId)
            user?.let {
                etName.setText(it.name)
                etEmail.setText(it.email)
            }
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = database.userDao().getUserById(userId)
                if (user != null) {
                    val updatedUser = user.copy(name = name, email = email)
                    database.userDao().insertUser(updatedUser)
                    
                    // Update shared prefs too if needed
                    sharedPref.edit().putString("userName", name).apply()
                    
                    Toast.makeText(this@EditProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}
