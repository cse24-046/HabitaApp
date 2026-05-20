package com.example.habita.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.habita.R
import com.example.habita.adapters.ImageSliderAdapter
import com.example.habita.adapters.ListingAdapter
import com.example.habita.database.AppDatabase
import com.example.habita.database.Listing
import com.example.habita.utils.NotificationHelper
import com.example.habita.utils.SampleData
import com.google.android.material.slider.RangeSlider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private var allListings: List<Listing> = listOf()
    private lateinit var recyclerListings: RecyclerView
    private lateinit var notificationHelper: NotificationHelper
    
    private fun updateUI(newList: List<Listing>) {
        recyclerListings.adapter = ListingAdapter(newList, 
            onItemClick = { listing ->
                showImageSliderDialog(listing)
            },
            onFavoriteClick = { listing ->
                lifecycleScope.launch {
                    val updatedListing = listing.copy(isSaved = !listing.isSaved)
                    database.listingDao().updateListing(updatedListing)
                    allListings = allListings.map { if (it.id == listing.id) updatedListing else it }
                    applyFilters()
                    Toast.makeText(this@HomeActivity, if (updatedListing.isSaved) "Added to Favorites" else "Removed", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        recyclerListings = findViewById(R.id.recyclerListings)
        recyclerListings.layoutManager = LinearLayoutManager(this)

        val locationSpinner = findViewById<Spinner>(R.id.locationSpinner)
        val houseTypeSpinner = findViewById<Spinner>(R.id.houseTypeSpinner)
        val priceSlider = findViewById<RangeSlider>(R.id.priceRangeSlider)
        val searchBar = findViewById<EditText>(R.id.searchBar)

        val locations = resources.getStringArray(R.array.locations_array).toMutableList().apply { add(0, "Any") }
        locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val houseTypes = resources.getStringArray(R.array.house_types_array).toMutableList().apply { add(0, "Any") }
        houseTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)

        priceSlider.setValues(sharedPref.getFloat("minPrice", 0f), sharedPref.getFloat("maxPrice", 5000f))
        locationSpinner.setSelection(locations.indexOf(sharedPref.getString("location", "Any")).coerceAtLeast(0))
        houseTypeSpinner.setSelection(houseTypes.indexOf(sharedPref.getString("houseType", "Any")).coerceAtLeast(0))

        findViewById<ImageButton>(R.id.navHome).setOnClickListener { }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navChat).setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        lifecycleScope.launch {
            val dao = database.listingDao()
            val existing = dao.getAllListings().first()
            if (existing.size < 65) {
                dao.insertAll(SampleData.get65Listings())
            }
            dao.getAllListings().collect { listings ->
                allListings = listings
                applyFilters()
                checkForNewMatches(listings)
            }
        }

        searchBar.addTextChangedListener { applyFilters() }
        priceSlider.addOnChangeListener { _, _, _ -> applyFilters() }
        
        val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) { applyFilters() }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        locationSpinner.onItemSelectedListener = itemSelectedListener
        houseTypeSpinner.onItemSelectedListener = itemSelectedListener

        findViewById<Button>(R.id.btnSavePreferences).setOnClickListener {
            val isLocked = sharedPref.getBoolean("isLocked", false)
            if (isLocked) {
                Toast.makeText(this, "Preferences are locked in Settings!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            with(sharedPref.edit()) {
                putFloat("minPrice", priceSlider.values[0])
                putFloat("maxPrice", priceSlider.values[1])
                putString("location", locationSpinner.selectedItem.toString())
                putString("houseType", houseTypeSpinner.selectedItem.toString())
                apply()
            }
            Toast.makeText(this, "Preferences Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilters() {
        val searchBar = findViewById<EditText>(R.id.searchBar)
        val priceSlider = findViewById<RangeSlider>(R.id.priceRangeSlider)
        val locationSpinner = findViewById<Spinner>(R.id.locationSpinner)
        val houseTypeSpinner = findViewById<Spinner>(R.id.houseTypeSpinner)

        val query = searchBar.text.toString().lowercase()
        val min = priceSlider.values[0].toInt()
        val max = priceSlider.values[1].toInt()
        val loc = locationSpinner.selectedItem?.toString() ?: "Any"
        val type = houseTypeSpinner.selectedItem?.toString() ?: "Any"

        val filtered = allListings.filter { listing ->
            (listing.title.lowercase().contains(query) || listing.location.lowercase().contains(query)) &&
            (listing.price in min..max) &&
            (loc == "Any" || listing.location == loc) &&
            (type == "Any" || listing.houseType == type)
        }
        updateUI(filtered)
    }

    private fun showImageSliderDialog(listing: Listing) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_listing_images)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewPagerImages)
        val tabLayout = dialog.findViewById<TabLayout>(R.id.tabDots)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        
        // Correcting the button setup
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

    private fun checkForNewMatches(listings: List<Listing>) {
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val loc = sharedPref.getString("location", "Any")
        val type = sharedPref.getString("houseType", "Any")
        
        if (loc == "Any" && type == "Any") return

        val matches = listings.filter { 
            (loc == "Any" || it.location == loc) && 
            (type == "Any" || it.houseType == type) &&
            it.status == "Available"
        }
        
        if (matches.isNotEmpty()) {
            notificationHelper.showMatchNotification(
                "Habita Matches Found!",
                "We found ${matches.size} new homes in $loc matching your $type preference."
            )
        }
    }
}