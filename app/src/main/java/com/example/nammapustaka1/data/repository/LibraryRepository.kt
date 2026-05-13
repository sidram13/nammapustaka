package com.example.nammapustaka1.data.repository

import androidx.lifecycle.LiveData
import com.example.nammapustaka1.data.dao.*
import com.example.nammapustaka1.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val bookDao: BookDao,
    private val studentDao: StudentDao,
    private val transactionDao: TransactionDao,
    private val reviewDao: ReviewDao
) {

    // ─── Books ───────────────────────────────────────────────────────────────

    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()
    val allCategories: LiveData<List<String>> = bookDao.getAllCategories()
    val totalBookCount: LiveData<Int> = bookDao.getTotalBookCount()

    suspend fun insertBook(book: Book): Long = withContext(Dispatchers.IO) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: Book) = withContext(Dispatchers.IO) {
        bookDao.deleteBook(book)
    }

    fun getBookById(bookId: Long): LiveData<Book?> = bookDao.getBookById(bookId)

    fun getBooksByCategory(category: String): LiveData<List<Book>> =
        bookDao.getBooksByCategory(category)

    fun searchBooks(query: String): LiveData<List<Book>> = bookDao.searchBooks(query)

    suspend fun getBookByQrCode(qrCode: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByQrCode(qrCode)
    }

    suspend fun getBookByIsbn(isbn: String): Book? = withContext(Dispatchers.IO) {
        bookDao.getBookByIsbn(isbn)
    }

    // ─── Students ─────────────────────────────────────────────────────────────

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()

    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    fun getStudentById(studentId: Long): LiveData<Student?> = studentDao.getStudentById(studentId)

    suspend fun getStudentByRollNumber(rollNumber: String): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentByRollNumber(rollNumber)
    }

    fun getTopReadersByPages(limit: Int = 10): LiveData<List<Student>> =
        studentDao.getTopReadersByPages(limit)

    fun getTopReadersByBooks(limit: Int = 10): LiveData<List<Student>> =
        studentDao.getTopReadersByBooks(limit)

    fun searchStudents(query: String): LiveData<List<Student>> = studentDao.searchStudents(query)

    // ─── Transactions ─────────────────────────────────────────────────────────

    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val activeBorrowCount: LiveData<Int> = transactionDao.getActiveBorrowCount()

    suspend fun issueBook(transaction: Transaction): Long = withContext(Dispatchers.IO) {
        val id = transactionDao.insertTransaction(transaction)
        bookDao.decrementAvailableCopies(transaction.bookId)
        id
    }

    suspend fun returnBook(transactionId: Long, returnDate: Long, pagesRead: Int, studentId: Long, pages: Int) {
        withContext(Dispatchers.IO) {
            transactionDao.returnBook(transactionId, returnDate, pagesRead)
            val tx = transactionDao.getAllTransactions().value?.find { it.id == transactionId }
            tx?.let {
                bookDao.incrementAvailableCopies(it.bookId)
                studentDao.updateReadingStats(studentId, pages)
            }
        }
    }

    suspend fun markOverdue() = withContext(Dispatchers.IO) {
        transactionDao.markOverdueTransactions()
    }

    fun getTransactionsByStudent(studentId: Long): LiveData<List<Transaction>> =
        transactionDao.getTransactionsByStudent(studentId)

    fun getActiveTransactionsByStudent(studentId: Long): LiveData<List<Transaction>> =
        transactionDao.getActiveTransactionsByStudent(studentId)

    fun getTransactionsByStatus(status: String): LiveData<List<Transaction>> =
        transactionDao.getTransactionsByStatus(status)

    suspend fun getActiveTransactionByBook(bookId: Long): Transaction? = withContext(Dispatchers.IO) {
        transactionDao.getActiveTransactionByBook(bookId)
    }

    // ─── Reviews ──────────────────────────────────────────────────────────────

    fun getReviewsByBook(bookId: Long): LiveData<List<Review>> = reviewDao.getReviewsByBook(bookId)

    fun getAverageRatingForBook(bookId: Long): LiveData<Float?> =
        reviewDao.getAverageRatingForBook(bookId)

    fun getReviewCountForBook(bookId: Long): LiveData<Int> =
        reviewDao.getReviewCountForBook(bookId)

    suspend fun insertReview(review: Review): Long = withContext(Dispatchers.IO) {
        reviewDao.insertReview(review)
    }

    suspend fun getReviewByBookAndStudent(bookId: Long, studentId: Long): Review? =
        withContext(Dispatchers.IO) {
            reviewDao.getReviewByBookAndStudent(bookId, studentId)
        }
}
