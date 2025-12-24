package com.example.ideajar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brain_signals")
data class BrainSignal(
    @PrimaryKey val word: String, // The word itself is the key
    val categoryScores: String // JSON Map<CategoryId, Frequency> e.g. "{1: 5, 2: 1}"
)
