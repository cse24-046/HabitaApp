package com.example.habita.activities

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
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
    private var selectedImageUri: Uri? = null
    private var isEditMode = false
    private var editingListingId = -1

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            val txtImageName = findViewById<TextView>(R.id.txtUploadedImageName)
            val imgPreview = findViewById<ImageView>(R.id.imgUploadPreview)
            
            try {
                // Grant persistent access to the URI for later use
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            txtImageName.text = "Selected Image"
            imgPreview.setImageURI(uri)
            imgPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_listing)

        database = AppDatabase.getDatabase(this)
        
        editingListingId = intent.getIntExtra("listingId", -1)
        isEditMode = editingListingId != -1

        val etTitle = findViewById<EditText>(R.id.etUploadTitle)
        val etPrice = findViewById<EditText>(R.id.etUploadPrice)
        val spinnerLocation = findViewById<Spinner>(R.id.spinnerUploadLocation)
        val spinnerHouseType = findViewById<Spinner>(R.id.spinnerUploadHouseType)
        etDate = findViewById(R.id.etUploadDate)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitUpload)
        val btnBack = findViewById<ImageButton>(R.id.btnBackToDashboard)
        val btnSelectImage = findViewById<Button>(R.id.btnUploadSelectImage)
        val txtHeaderTitle = findViewById<TextView>(R.id.txtHeaderTitle)

        if (isEditMode) {
            btnSubmit.text = "Update Listing"
            txtHeaderTitle.text = "Edit Property"
        }

        val locations = resources.getStringArray(R.array.locations_array)
        val adapterLocation = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        spinnerLocation.adapter = adapterLocation

        val houseTypes = resources.getStringArray(R.array.house_types_array)
        val adapterHouseType = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, houseTypes)
        spinnerHouseType.adapter = adapterHouseType

        if (isEditMode) {
            lifecycleScope.launch {
                val listing = database.listingDao().getListingById(editingListingId)
                listing?.let {
                    etTitle.setText(it.title)
                    etPrice.setText(it.price.toString())
                    etDate.setText(it.availabilityDate)
                    spinnerLocation.setSelection(locations.indexOf(it.location).coerceAtLeast(0))
                    spinnerHouseType.setSelection(houseTypes.indexOf(it.houseType).coerceAtLeast(0))
                    if (it.mainImage != null) {
                        val imgPreview = findViewById<ImageView>(R.id.imgUploadPreview)
                        try {
                            val resId = it.mainImage!!.toIntOrNull()
                            if (resId != null) imgPreview.setImageResource(resId)
                            else imgPreview.setImageURI(Uri.parse(it.mainImage))
                            imgPreview.visibility = View.VISIBLE
                        } catch (e: Exception) {}
                    }
                }
            }
        }

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
            selectImageLauncher.launch(arrayOf("image/*"))
        }

        btnBack.setOnClickListener { finish() }

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

            val price = priceStr.toIntOrNull() ?: 0

            lifecycleScope.launch {
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val providerId = sharedPref.getString("userId", null)

                var mainImg: String? = selectedImageUri?.toString()
                
                if (isEditMode) {
                    val existing = database.listingDao().getListingById(editingListingId)
                    val updatedMainImg = mainImg ?: existing?.mainImage
                    val updatedImgList = if (mainImg != null) listOf(mainImg) else existing?.imageList ?: emptyList()
                    
                    val updated = existing?.copy(
                        title = title, price = price, location = location,
                        houseType = houseType, availabilityDate = availabilityDate,
                        mainImage = updatedMainImg, imageList = updatedImgList
                    )
                    if (updated != null) database.listingDao().updateListing(updated)
                } else {
                    val finalMainImg = mainImg ?: R.drawable.house_1.toString()
                    val newListing = Listing(
                        title = title, price = price, location = location,
                        houseType = houseType, availabilityDate = availabilityDate,
                        status = "Available", mainImage = finalMainImg,
                        imageList = listOf(finalMainImg, R.drawable.house_inner_1.toString()),
                        providerId = providerId
                    )
                    database.listingDao().insertListing(newListing)
                }
                Toast.makeText(this@UploadListingActivity, if (isEditMode) "Listing Updated" else "Listing Uploaded", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
