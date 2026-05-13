package com.example.nammapustaka1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val category: String,           // Story, Science, History, etc.
    val isbn: String = "",
    val totalPages: Int = 0,
    val coverUrl: String = "",      // Open Library cover URL
    val description: String = "",   // Gemini-generated summary (Kannada)
    val qrCode: String = "",        // Unique QR identifier
    val availableCopies: Int = 1,
    val totalCopies: Int = 1,
    val addedDate: Long = System.currentTimeMillis(),
    val language: String = "Kannada"
)
