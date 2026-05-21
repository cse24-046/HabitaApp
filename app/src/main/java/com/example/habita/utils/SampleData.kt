package com.example.habita.utils

import com.example.habita.database.Listing
import com.example.habita.R
import java.util.Locale

object SampleData {
    fun get65Listings(): List<Listing> {
        val locations = listOf("Gaborone", "Tlokweng", "Broadhurst", "G-West", "Phakalane", "Block 6", "Mogoditshane", "Kgale")
        val types = listOf("Single Room", "Bachelor Pad", "One Bedroom Flat", "Shared Apartment", "Studio")
        val listings = mutableListOf<Listing>()
        
        // Simulating high-quality AI images using built-in drawables as placeholders
        val dummyImages = listOf(
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_camera,
            android.R.drawable.ic_menu_today,
            android.R.drawable.ic_dialog_info,
            android.R.drawable.ic_menu_slideshow
        )
        
        for (i in 1..65) {
            val loc = locations[i % locations.size]
            val type = types[i % types.size]
            val price = 400 + (i * 145) % 6000
            val day = (i % 28) + 1
            val monthIdx = (i / 4) % 4
            val months = listOf("May", "June", "July", "August")
            val month = months[monthIdx]
            
            // Randomly mark some as RESERVED
            val status = if (i % 6 == 0) "RESERVED" else "Available"
            
            // Assign a main image and a list of images for the slider
            val mainImg = dummyImages[i % dummyImages.size]
            val extraImgs = listOf(mainImg, dummyImages[(i+1) % dummyImages.size], dummyImages[(i+2) % dummyImages.size])
            
            listings.add(
                Listing(
                    id = i,
                    title = "Premium AI-Designed $type in $loc #$i",
                    price = price,
                    location = loc,
                    availabilityDate = String.format(Locale.getDefault(), "%02d %s 2026", day, month),
                    houseType = type,
                    status = status,
                    mainImage = mainImg,
                    imageList = extraImgs
                )
            )
        }
        return listings
    }
}