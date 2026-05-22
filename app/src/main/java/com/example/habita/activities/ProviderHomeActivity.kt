package com.example.habita.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.adapters.ListingAdapter
import com.example.habita.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProviderHomeActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerListings: RecyclerView
    private lateinit var txtListingCount: TextView
    private lateinit var txtWelcomeSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_home)

        database = AppDatabase.getDatabase(this)

        // Bind views
        recyclerListings = findViewById(R.id.recyclerProviderListings)
        recyclerListings.layoutManager = GridLayoutManager(this, 2)
        txtListingCount = findViewById(R.id.txtListingCount)
        txtWelcomeSubtitle = findViewById(R.id.providerNameSubtitle)

        val btnLogout = findViewById<ImageButton>(R.id.btnProviderLogout)
        val btnChat = findViewById<ImageButton>(R.id.btnProviderChat)
        val btnProfile = findViewById<ImageButton>(R.id.btnProviderProfile)
        val cardQuickAdd = findViewById<CardView>(R.id.cardQuickAdd)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddListing)

        btnChat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Load provider userId
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        // Set up list and stats observer
        lifecycleScope.launch {
            database.listingDao().getAllListings().collectLatest { listings ->
                // Filtering listings uploaded by this provider
                val providerListings = listings.filter { it.providerId == userId }
                txtListingCount.text = providerListings.size.toString()
                recyclerListings.adapter = ListingAdapter(
                    listings = providerListings,
                    onItemClick = { listing ->
                        // Allow provider to edit the listing they uploaded
                        val intent = Intent(this@ProviderHomeActivity, UploadListingActivity::class.java)
                        intent.putExtra("listingId", listing.id)
                        startActivity(intent)
                    },
                    onFavoriteClick = { listing ->
                        lifecycleScope.launch {
                            val updatedListing = listing.copy(isSaved = !listing.isSaved)
                            database.listingDao().updateListing(updatedListing)
                        }
                    }
                )
            }
        }

        // Navigate to Upload listing
        val toUploadLambda = {
            startActivity(Intent(this, UploadListingActivity::class.java))
        }
        cardQuickAdd.setOnClickListener { toUploadLambda() }
        fabAdd.setOnClickListener { toUploadLambda() }

        // Logout
        btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }
    }
}
