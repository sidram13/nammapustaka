package com.example.nammapustaka1.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nammapustaka1.data.model.Transaction
import com.example.nammapustaka1.data.model.TransactionStatus

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY issueDate DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE studentId = :studentId ORDER BY issueDate DESC")
    fun getTransactionsByStudent(studentId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE bookId = :bookId ORDER BY issueDate DESC")
    fun getTransactionsByBook(bookId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY issueDate DESC")
    fun getTransactionsByStatus(status: String): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE studentId = :studentId AND status IN (:activeStatuses)")
    fun getActiveTransactionsByStudent(
        studentId: Long,
        activeStatuses: List<String> = listOf(TransactionStatus.ISSUED, TransactionStatus.OVERDUE)
    ): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE bookId = :bookId AND status IN ('ISSUED','OVERDUE') LIMIT 1")
    suspend fun getActiveTransactionByBook(bookId: Long): Transaction?

    @Query("UPDATE transactions SET status = 'OVERDUE' WHERE dueDate < :now AND status = 'ISSUED'")
    suspend fun markOverdueTransactions(now: Long = System.currentTimeMillis())

    @Query("UPDATE transactions SET returnDate = :returnDate, status = 'RETURNED', pagesRead = :pagesRead WHERE id = :transactionId")
    suspend fun returnBook(transactionId: Long, returnDate: Long, pagesRead: Int)

    @Query("SELECT COUNT(*) FROM transactions WHERE status IN ('ISSUED','OVERDUE')")
    fun getActiveBorrowCount(): LiveData<Int>

    @Query("SELECT * FROM transactions WHERE dueDate < :now AND status = 'ISSUED'")
    suspend fun getOverdueTransactions(now: Long = System.currentTimeMillis()): List<Transaction>
}
