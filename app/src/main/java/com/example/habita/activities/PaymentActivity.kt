package com.example.habita.activities

import android.content.Intent
import android.os.Bundle
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
        val btnPay = findViewById<Button>(R.id.btnPay)
        
        val etCardNumber = findViewById<EditText>(R.id.editCardNumber)
        val etExpiry = findViewById<EditText>(R.id.editExpiry)
        val etCVV = findViewById<EditText>(R.id.editCVV)

        val title = intent.getStringExtra("title") ?: "Housing"
        val price = intent.getIntExtra("price", 0)
        val deposit = price / 2

        txtPaymentTitle.text = title
        txtPaymentAmount.text = "Deposit: P$deposit"

        btnBack.setOnClickListener { finish() }

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

        btnPay.setOnClickListener {
            val cardNum = etCardNumber.text.toString().trim()
            val expiry = etExpiry.text.toString().trim()
            val cvv = etCVV.text.toString().trim()

            if (cardNum.length < 16 || expiry.isEmpty() || cvv.length < 3) {
                Toast.makeText(this, "Please enter valid card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reference = "HB" + System.currentTimeMillis().toString().takeLast(6)

            lifecycleScope.launch {
                val dao = database.listingDao()
                val listing = dao.getListingByTitle(title)
                if (listing != null) {
                    // Permanently mark the listing as RESERVED
                    dao.updateListing(listing.copy(status = "RESERVED"))
                }

                // Record the confirmed booking
                database.bookingDao().saveBooking(Booking(title = title, reference = reference, status = "RESERVED"))

                val intent = Intent(this@PaymentActivity, SuccessActivity::class.java)
                intent.putExtra("reference", reference)
                startActivity(intent)
                finish()
            }
        }
    }
}