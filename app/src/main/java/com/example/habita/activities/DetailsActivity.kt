package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.habita.R
import com.example.habita.adapters.ImageSliderAdapter
import com.example.habita.database.AppDatabase
import com.example.habita.database.Listing
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailsActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private var currentListing: Listing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        database = AppDatabase.getDatabase(this)
        val listingId = intent.getIntExtra("listingId", -1)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtDetailsTitle)
        val txtPrice = findViewById<TextView>(R.id.txtDetailsPrice)
        val txtLocation = findViewById<TextView>(R.id.txtDetailsLocation)
        val txtDate = findViewById<TextView>(R.id.txtDetailsDate)
        val btnReserve = findViewById<Button>(R.id.btnReserve)
        val btnChat = findViewById<Button>(R.id.btnChat)
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerDetails)
        val tabLayout = findViewById<TabLayout>(R.id.tabDotsDetails)

        // Navigation
        findViewById<ImageButton>(R.id.navHome).setOnClickListener { 
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val role = sharedPref.getString("userRole", "student")
            if (role == "provider") {
                startActivity(Intent(this, ProviderHomeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish() 
        }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener { startActivity(Intent(this, SavedActivity::class.java)); finish() }
        findViewById<ImageButton>(R.id.navChat).setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)); finish() }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)); finish() }

        lifecycleScope.launch {
            val listingsList = database.listingDao().getAllListings().first()
            val listing = listingsList.find { it.id == listingId }
            if (listing != null) {
                currentListing = listing
                txtTitle.text = listing.title
                txtPrice.text = "P${listing.price} / month"
                txtLocation.text = listing.location
                txtDate.text = "Available From: ${listing.availabilityDate}"

                val images = if (listing.imageList.isNotEmpty()) {
                    listing.imageList
                } else if (listing.mainImage != null) {
                    listOf(listing.mainImage, android.R.drawable.ic_menu_gallery.toString())
                } else {
                    listOf(R.mipmap.ic_launcher.toString(), android.R.drawable.ic_menu_gallery.toString())
                }
                
                viewPager.adapter = ImageSliderAdapter(images)
                TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
                
                if (listing.status == "RESERVED") {
                    btnReserve.isEnabled = false
                    btnReserve.text = getString(R.string.reserved)
                }
            } else {
                Toast.makeText(this@DetailsActivity, "Listing not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        btnBack.setOnClickListener { finish() }

        btnReserve.setOnClickListener {
            currentListing?.let {
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("title", it.title)
                intent.putExtra("price", it.price)
                startActivity(intent)
            }
        }

        btnChat.setOnClickListener {
            currentListing?.let {
                val intent = Intent(this, ChatDetailActivity::class.java)
                intent.putExtra("partnerName", it.location + " Provider")
                startActivity(intent)
            }
        }
    }
}
