package com.example.habita.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class Preference(
    @PrimaryKey val userId: String,
    val minPrice: Int = 0,
    val maxPrice: Int = 5000,
    val location: String = "Any",
    val houseType: String = "Any",
    val preferredDate: String = "",
    val isLocked: Boolean = false
)