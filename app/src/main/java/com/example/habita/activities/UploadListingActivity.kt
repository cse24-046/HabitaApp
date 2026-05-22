package com.example.habita.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import com.example.habita.database.Listing
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UploadListingActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private val calendar = Calendar.getInstance()
    private lateinit var etDate: EditText
    private var selectedImageUri: android.net.Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val txtImageName = findViewById<TextView>(R.id.txtUploadedImageName)
            val imgPreview = findViewById<ImageView>(R.id.imgUploadPreview)
            
            // Persist permission to access this URI later (e.g. after app restart)
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            txtImageName.text = "Selected: Property_Image_${System.currentTimeMillis() % 10000}.jpg"
            imgPreview.setImageURI(uri)
            imgPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_listing)

        database = AppDatabase.getDatabase(this)

        val etTitle = findViewById<EditText>(R.id.etUploadTitle)
        val etPrice = findViewById<EditText>(R.id.etUploadPrice)
        val spinnerLocation = findViewById<Spinner>(R.id.spinnerUploadLocation)
        val spinnerHouseType = findViewById<Spinner>(R.id.spinnerUploadHouseType)
        etDate = findViewById(R.id.etUploadDate)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitUpload)
        val btnBack = findViewById<ImageButton>(R.id.btnBackToDashboard)
        val btnSelectImage = findViewById<Button>(R.id.btnUploadSelectImage)

        // Set up Spinners using the arrays in arrays.xml
        val locations = resources.getStringArray(R.array.locations_array)
        val adapterLocation = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocation.adapter = adapterLocation

        val houseTypes = resources.getStringArray(R.array.house_types_array)
        val adapterHouseType = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)
        spinnerHouseType.adapter = adapterHouseType

        // Set up DatePickerDialog
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            etDate.setText(format.format(calendar.time))
        }

        etDate.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val location = spinnerLocation.selectedItem.toString()
            val houseType = spinnerHouseType.selectedItem.toString()
            val availabilityDate = etDate.text.toString().trim()

            if (title.isEmpty() || priceStr.isEmpty() || availabilityDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toIntOrNull()
            if (price == null || price <= 0) {
                Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val providerId = sharedPref.getString("userId", null)

                // Use the URI if selected, otherwise a default resource string
                val mainImg = selectedImageUri?.toString() ?: R.drawable.house_1.toString()
                val extraImgs = if (selectedImageUri != null) listOf(mainImg) else listOf(
                    R.drawable.house_1.toString(),
                    R.drawable.house_inner_1.toString()
                )

                val newListing = Listing(
                    title = title,
                    price = price,
                    location = location,
                    availabilityDate = availabilityDate,
                    houseType = houseType,
                    status = "Available",
                    mainImage = mainImg,
                    imageList = extraImgs,
                    providerId = providerId
                )

                database.listingDao().insertListing(newListing)
                Toast.makeText(this@UploadListingActivity, "Listing Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
