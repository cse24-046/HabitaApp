package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.habita.R
import com.example.habita.database.AppDatabase
import com.example.habita.database.Booking
import kotlinx.coroutines.launch

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val database = AppDatabase.getDatabase(this)
        
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtPaymentTitle = findViewById<TextView>(R.id.txtPaymentTitle)
        val txtPaymentAmount = findViewById<TextView>(R.id.txtPaymentAmount)
        val radioPaymentMethod = findViewById<RadioGroup>(R.id.radioPaymentMethod)
        val btnPay = findViewById<Button>(R.id.btnPay)
        
        val layoutCardDetails = findViewById<LinearLayout>(R.id.layoutCardDetails)
        val layoutMobileDetails = findViewById<LinearLayout>(R.id.layoutMobileDetails)
        val spinnerMobileProvider = findViewById<Spinner>(R.id.spinnerMobileProvider)

        val title = intent.getStringExtra("title") ?: "Selected Room"
        val price = intent.getIntExtra("price", 0)
        val deposit = price / 2

        txtPaymentTitle.text = title
        txtPaymentAmount.text = "Deposit: P$deposit"

        btnBack.setOnClickListener {
            finish()
        }

        // Setup Mobile Providers
        val providers = arrayOf("Orange Money", "MyZaka", "Smega")
        spinnerMobileProvider.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)

        // Navigation for bottom bar
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

        // Handle dynamic UI visibility for payment methods
        radioPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioCard -> {
                    layoutCardDetails.visibility = View.VISIBLE
                    layoutMobileDetails.visibility = View.GONE
                }
                R.id.radioWallet -> {
                    layoutCardDetails.visibility = View.GONE
                    layoutMobileDetails.visibility = View.VISIBLE
                }
                else -> {
                    layoutCardDetails.visibility = View.GONE
                    layoutMobileDetails.visibility = View.GONE
                }
            }
        }

        btnPay.setOnClickListener {
            val selectedId = radioPaymentMethod.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please choose a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simple validation
            if (selectedId == R.id.radioCard) {
                val cardNum = findViewById<EditText>(R.id.editCardNumber).text.toString()
                if (cardNum.length < 16) {
                    Toast.makeText(this, "Please enter a valid card number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else if (selectedId == R.id.radioWallet) {
                val mobileNum = findViewById<EditText>(R.id.editMobileNumber).text.toString()
                if (mobileNum.isEmpty()) {
                    Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val reference = "HB" + System.currentTimeMillis().toString().takeLast(6)

            lifecycleScope.launch {
                // Update listing status to RESERVED
                val listing = database.listingDao().getListingByTitle(title)
                if (listing != null) {
                    database.listingDao().updateListing(listing.copy(status = "RESERVED"))
                }

                // Save record
                val booking = Booking(
                    title = title,
                    reference = reference,
                    status = "RESERVED"
                )
                database.bookingDao().saveBooking(booking)

                val intent = Intent(this@PaymentActivity, SuccessActivity::class.java)
                intent.putExtra("reference", reference)
                startActivity(intent)
                finish()
            }
        }
    }
}