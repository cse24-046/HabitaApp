package com.example.habita.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.habita.R
import com.example.habita.adapters.ImageSliderAdapter
import com.example.habita.adapters.ListingAdapter
import com.example.habita.database.AppDatabase
import com.example.habita.database.Listing
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class SavedActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerSaved: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        database = AppDatabase.getDatabase(this)
        recyclerSaved = findViewById(R.id.recyclerSaved)
        recyclerSaved.layoutManager = LinearLayoutManager(this)

        // Navigation
        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener { }
        findViewById<ImageButton>(R.id.navChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            database.listingDao().getSavedListings().collect { savedListings ->
                recyclerSaved.adapter = ListingAdapter(savedListings,
                    onItemClick = { listing ->
                        showImageSliderDialog(listing)
                    },
                    onFavoriteClick = { listing ->
                        lifecycleScope.launch {
                            database.listingDao().updateListing(listing.copy(isSaved = false))
                            Toast.makeText(this@SavedActivity, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    private fun showImageSliderDialog(listing: Listing) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_listing_images)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewPagerImages)
        val tabLayout = dialog.findViewById<TabLayout>(R.id.tabDots)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        
        val layout = btnClose.parent as LinearLayout
        val btnViewDetails = Button(this).apply {
            text = "View Full Details"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        }
        layout.addView(btnViewDetails, layout.indexOfChild(btnClose))

        val sampleImages = listOf(
            R.mipmap.ic_launcher,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_camera
        )

        viewPager.adapter = ImageSliderAdapter(sampleImages)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnViewDetails.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("title", listing.title)
                putExtra("price", listing.price)
                putExtra("location", listing.location)
                putExtra("date", listing.availabilityDate)
            }
            startActivity(intent)
        }

        dialog.show()
    }
}