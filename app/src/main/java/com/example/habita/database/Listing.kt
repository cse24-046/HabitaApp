package com.example.habita.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val price: Int,
    val location: String,
    val availabilityDate: String,
    val houseType: String,
    val isSaved: Boolean = false,
    val status: String = "Available", // "Available" or "RESERVED"
    val imageRes: Int = 0
)