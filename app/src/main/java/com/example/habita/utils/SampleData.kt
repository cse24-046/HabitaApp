package com.example.habita.utils

import com.example.habita.database.Listing
import java.util.Locale

object SampleData {
    fun get65Listings(): List<Listing> {
        val locations = listOf("Gaborone", "Tlokweng", "Broadhurst", "G-West", "Phakalane")
        val types = listOf("Single Room", "Bachelor Pad", "One Bedroom Flat", "Shared Apartment")
        val listings = mutableListOf<Listing>()
        
        for (i in 1..65) {
            val loc = locations[i % locations.size]
            val type = types[i % types.size]
            val price = 500 + (i * 130) % 4500
            val day = (i % 28) + 1
            val month = if (i % 2 == 0) "May" else "June"
            listings.add(
                Listing(
                    title = "$loc $type #$i",
                    price = price,
                    location = loc,
                    availabilityDate = String.format(Locale.getDefault(), "%02d %s 2026", day, month),
                    houseType = type,
                    status = if (i % 10 == 0) "RESERVED" else "Available"
                )
            )
        }
        return listings
    }
}