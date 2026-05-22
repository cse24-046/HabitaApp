package com.example.habita.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
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
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private var allListings: List<Listing> = listOf()
    private lateinit var recyclerListings: RecyclerView
    private lateinit var notificationHelper: NotificationHelper
    private val calendar = Calendar.getInstance()

    private lateinit var locationSpinner: Spinner
    private lateinit var houseTypeSpinner: Spinner
    private lateinit var priceSlider: RangeSlider
    private lateinit var searchBar: EditText
    private lateinit var etPreferredDate: EditText
    private lateinit var switchLock: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        database = AppDatabase.getDatabase(this)
        notificationHelper = NotificationHelper(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        recyclerListings = findViewById(R.id.recyclerListings)
        recyclerListings.layoutManager = LinearLayoutManager(this)

        // Initialize UI Components
        locationSpinner = findViewById(R.id.locationSpinner)
        houseTypeSpinner = findViewById(R.id.houseTypeSpinner)
        priceSlider = findViewById(R.id.priceRangeSlider)
        searchBar = findViewById(R.id.searchBar)
        etPreferredDate = findViewById(R.id.etPreferredDate)
        switchLock = findViewById(R.id.switchLockPreferences)

        val locations = resources.getStringArray(R.array.locations_array).toMutableList().apply { add(0, "Any") }
        locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val houseTypes = resources.getStringArray(R.array.house_types_array).toMutableList().apply { add(0, "Any") }
        houseTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)

        // Load saved preferences
        val isLocked = sharedPref.getBoolean("isLocked", false)
        switchLock.isChecked = isLocked
        priceSlider.setValues(sharedPref.getFloat("minPrice", 0f), sharedPref.getFloat("maxPrice", 10000f))
        locationSpinner.setSelection(locations.indexOf(sharedPref.getString("location", "Any")).coerceAtLeast(0))
        houseTypeSpinner.setSelection(houseTypes.indexOf(sharedPref.getString("houseType", "Any")).coerceAtLeast(0))
        etPreferredDate.setText(sharedPref.getString("prefDate", ""))

        if (isLocked) disableControls()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            etPreferredDate.setText(format.format(calendar.time))
            applyFilters()
        }

        etPreferredDate.setOnClickListener {
            if (!switchLock.isChecked) {
                DatePickerDialog(this, dateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener { }
        findViewById<ImageButton>(R.id.navSaved).setOnClickListener { startActivity(Intent(this, SavedActivity::class.java)) }
        findViewById<ImageButton>(R.id.navChat).setOnClickListener { startActivity(Intent(this, ChatActivity::class.java)) }
        findViewById<ImageButton>(R.id.navProfile).setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }

        lifecycleScope.launch {
            val dao = database.listingDao()
            if (dao.getSampleListings().first().size < 65) {
                dao.insertAll(SampleData.get65Listings())
            }
            dao.getSampleListings().collect { listings ->
                allListings = listings
                applyFilters()
                if (isLocked) checkForNewMatches(listings)
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
            val locked = switchLock.isChecked
            with(sharedPref.edit()) {
                putFloat("minPrice", priceSlider.values[0])
                putFloat("maxPrice", priceSlider.values[1])
                putString("location", locationSpinner.selectedItem.toString())
                putString("houseType", houseTypeSpinner.selectedItem.toString())
                putString("prefDate", etPreferredDate.text.toString())
                putBoolean("isLocked", locked)
                apply()
            }
            if (locked) disableControls() else enableControls()
            Toast.makeText(this, if (locked) "Search Criteria Locked" else "Preferences Saved", Toast.LENGTH_SHORT).show()
            applyFilters()
        }
    }

    private fun disableControls() {
        locationSpinner.isEnabled = false
        houseTypeSpinner.isEnabled = false
        priceSlider.isEnabled = false
        etPreferredDate.isEnabled = false
    }

    private fun enableControls() {
        locationSpinner.isEnabled = true
        houseTypeSpinner.isEnabled = true
        priceSlider.isEnabled = true
        etPreferredDate.isEnabled = true
    }

    private fun applyFilters() {
        val query = searchBar.text.toString().lowercase()
        val min = if (priceSlider.values.isNotEmpty()) priceSlider.values[0].toInt() else 0
        val max = if (priceSlider.values.size > 1) priceSlider.values[1].toInt() else 10000
        val loc = locationSpinner.selectedItem?.toString() ?: "Any"
        val type = houseTypeSpinner.selectedItem?.toString() ?: "Any"
        val date = etPreferredDate.text.toString()

        val filtered = allListings.filter { listing ->
            (listing.title.lowercase().contains(query) || listing.location.lowercase().contains(query)) &&
            (listing.price in min..max) &&
            (loc == "Any" || listing.location == loc) &&
            (type == "Any" || listing.houseType == type) &&
            (date.isEmpty() || listing.availabilityDate.contains(date, ignoreCase = true))
        }
        updateRecyclerView(filtered)
    }

    private fun updateRecyclerView(newList: List<Listing>) {
        recyclerListings.adapter = ListingAdapter(newList, 
            onItemClick = { listing ->
                if (listing.status == "RESERVED") {
                    Toast.makeText(this, "This home is already reserved and details are private.", Toast.LENGTH_SHORT).show()
                } else {
                    showImageSliderDialog(listing)
                }
            },
            onFavoriteClick = { listing ->
                lifecycleScope.launch {
                    val updatedListing = listing.copy(isSaved = !listing.isSaved)
                    database.listingDao().updateListing(updatedListing)
                    allListings = allListings.map { if (it.id == listing.id) updatedListing else it }
                    applyFilters()
                }
            }
        )
    }

    private fun showImageSliderDialog(listing: Listing) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_listing_images)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val viewPager = dialog.findViewById<ViewPager2>(R.id.viewPagerImages)
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        
        val layout = btnClose.parent as LinearLayout
        val btnViewDetails = Button(this).apply {
            text = getString(R.string.view_full_details)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 8 }
        }
        layout.addView(btnViewDetails, 0)

        // Fixed: Use imageList or mainImage correctly instead of imageRes
        val sliderImages = if (listing.imageList.isNotEmpty()) {
            listing.imageList
        } else if (listing.mainImage != 0) {
            listOf(listing.mainImage, android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_camera)
        } else {
            listOf(R.mipmap.ic_launcher, android.R.drawable.ic_menu_gallery, android.R.drawable.ic_menu_camera)
        }

        viewPager.adapter = ImageSliderAdapter(sliderImages)
        TabLayoutMediator(dialog.findViewById(R.id.tabDots), viewPager) { _, _ -> }.attach()

        btnClose.setOnClickListener { dialog.dismiss() }
        btnViewDetails.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, DetailsActivity::class.java).apply {
                putExtra("listingId", listing.id)
            })
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
                "Matches Found!",
                "We found ${matches.size} homes matching your criteria."
            )
        }
    }
}
