package com.example.habita.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, 
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student"
)