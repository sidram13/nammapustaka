package com.example.nammapustaka1.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookId"), Index("studentId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookId: Long,
    val studentId: Long,
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (14 * 24 * 60 * 60 * 1000L), // 14 days
    val returnDate: Long? = null,
    val status: String = TransactionStatus.ISSUED,  // ISSUED, RETURNED, OVERDUE
    val pagesRead: Int = 0,
    val qrScanCode: String = ""
)

object TransactionStatus {
    const val ISSUED = "ISSUED"
    const val RETURNED = "RETURNED"
    const val OVERDUE = "OVERDUE"
    const val RESERVED = "RESERVED"
}
