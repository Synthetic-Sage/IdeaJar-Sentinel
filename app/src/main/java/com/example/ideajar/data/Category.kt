package com.example.ideajar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: Long, // Store color as Long (ARGB)
    val xPos: Float, // Relative position (0.0 to 1.0)
    val yPos: Float
)
