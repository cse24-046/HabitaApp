package com.example.habita.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val reference: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)