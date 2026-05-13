package com.example.nammapustaka1.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rollNumber: String,
    val className: String,          // e.g. "Class 6"
    val section: String = "A",
    val profilePhotoPath: String = "",
    val totalPagesRead: Int = 0,
    val booksRead: Int = 0,
    val joinedDate: Long = System.currentTimeMillis()
)
