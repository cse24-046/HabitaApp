package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import com.example.habita.database.Booking
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val database = AppDatabase.getDatabase(this)
        
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtDetailsTitle)
        val txtPrice = findViewById<TextView>(R.id.txtDetailsPrice)
        val txtLocation = findViewById<TextView>(R.id.txtDetailsLocation)
        val txtDate = findViewById<TextView>(R.id.txtDetailsDate)
        val btnReserve = findViewById<Button>(R.id.btnReserve)
        val btnChat = findViewById<Button>(R.id.btnChat)

        // Bottom Navigation Icons
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

        val title = intent.getStringExtra("title") ?: ""
        val price = intent.getIntExtra("price", 0)
        val location = intent.getStringExtra("location") ?: ""
        val date = intent.getStringExtra("date") ?: ""

        txtTitle.text = title
        txtPrice.text = "P$price / month"
        txtLocation.text = location
        txtDate.text = "Available: $date"

        btnBack.setOnClickListener {
            finish()
        }

        btnReserve.setOnClickListener {
            lifecycleScope.launch {
                val reference = "HB" + System.currentTimeMillis().toString().takeLast(6)
                val booking = Booking(title = title, reference = reference, status = "Confirmed")
                database.bookingDao().saveBooking(booking)
                
                val intent = Intent(this@DetailsActivity, PaymentActivity::class.java)
                intent.putExtra("title", title)
                intent.putExtra("price", price)
                startActivity(intent)
            }
        }

        btnChat.setOnClickListener {
            val intent = Intent(this, ChatDetailActivity::class.java)
            intent.putExtra("partnerName", "Landlord - $title")
            startActivity(intent)
        }
    }
}