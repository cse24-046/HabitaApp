package com.example.habita.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.habita.utils.Converters

@Entity(tableName = "listings")
@TypeConverters(Converters::class)
data class Listing(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val price: Int,
    val location: String,
    val availabilityDate: String,
    val houseType: String,
    val isSaved: Boolean = false,
    val status: String = "Available", // "Available" or "RESERVED"
    val mainImage: Int = 0,
    val imageList: List<Int> = emptyList(),
    val isSample: Boolean = false,
    val providerId: String? = null
)