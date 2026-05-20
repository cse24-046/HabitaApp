package com.example.habita.activities

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import com.example.habita.database.Preference
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PreferencesActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        database = AppDatabase.getDatabase(this)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getString("userId", "default_user") ?: "default_user"

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val priceSlider = findViewById<RangeSlider>(R.id.priceRangeSliderPref)
        val locationSpinner = findViewById<Spinner>(R.id.locationSpinnerPref)
        val houseTypeSpinner = findViewById<Spinner>(R.id.houseTypeSpinnerPref)
        val etPreferredDate = findViewById<EditText>(R.id.etPreferredDate)
        val switchLock = findViewById<Switch>(R.id.switchLockPref)
        val btnSave = findViewById<Button>(R.id.btnSavePrefPage)

        // Setup Spinners
        val locations = resources.getStringArray(R.array.locations_array).toMutableList().apply { add(0, "Any") }
        locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)

        val houseTypes = resources.getStringArray(R.array.house_types_array).toMutableList().apply { add(0, "Any") }
        houseTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)

        // Date Picker
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            etPreferredDate.setText(format.format(calendar.time))
        }

        etPreferredDate.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Load Preferences
        lifecycleScope.launch {
            val pref = database.preferenceDao().getPreferences(currentUserId).first()
            pref?.let {
                priceSlider.setValues(it.minPrice.toFloat(), it.maxPrice.toFloat())
                locationSpinner.setSelection(locations.indexOf(it.location).coerceAtLeast(0))
                houseTypeSpinner.setSelection(houseTypes.indexOf(it.houseType).coerceAtLeast(0))
                etPreferredDate.setText(it.preferredDate)
                switchLock.isChecked = it.isLocked
                
                // If locked, disable inputs
                if (it.isLocked) {
                    priceSlider.isEnabled = false
                    locationSpinner.isEnabled = false
                    houseTypeSpinner.isEnabled = false
                    etPreferredDate.isEnabled = false
                    // Keep switch enabled so they can unlock
                }
            }
        }

        btnSave.setOnClickListener {
            val newPref = Preference(
                userId = currentUserId,
                minPrice = priceSlider.values[0].toInt(),
                maxPrice = priceSlider.values[1].toInt(),
                location = locationSpinner.selectedItem.toString(),
                houseType = houseTypeSpinner.selectedItem.toString(),
                preferredDate = etPreferredDate.text.toString(),
                isLocked = switchLock.isChecked
            )

            lifecycleScope.launch {
                database.preferenceDao().savePreferences(newPref)
                Toast.makeText(this@PreferencesActivity, "Preferences Updated", Toast.LENGTH_SHORT).show()
                if (newPref.isLocked) finish() // Close if locked
            }
        }

        btnBack.setOnClickListener { finish() }
    }
}