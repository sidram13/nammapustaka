package com.example.nammapustaka1.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatDate(millis: Long): String = dateFormat.format(Date(millis))

    fun formatDateTime(millis: Long): String = timeFormat.format(Date(millis))

    fun isOverdue(dueDate: Long): Boolean = System.currentTimeMillis() > dueDate

    fun daysOverdue(dueDate: Long): Int {
        val diff = System.currentTimeMillis() - dueDate
        return if (diff > 0) (diff / (24 * 60 * 60 * 1000)).toInt() else 0
    }

    fun daysRemaining(dueDate: Long): Int {
        val diff = dueDate - System.currentTimeMillis()
        return if (diff > 0) (diff / (24 * 60 * 60 * 1000)).toInt() else 0
    }
}
