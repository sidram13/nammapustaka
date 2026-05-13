package com.example.nammapustaka1.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.nammapustaka1.data.db.AppDatabase
import com.example.nammapustaka1.data.model.*
import com.example.nammapustaka1.data.repository.LibraryRepository
import kotlinx.coroutines.launch

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LibraryRepository

    // Books
    val allBooks: LiveData<List<Book>>
    val allCategories: LiveData<List<String>>
    val totalBookCount: LiveData<Int>

    // Students
    val allStudents: LiveData<List<Student>>
    val topReadersByPages: LiveData<List<Student>>
    val topReadersByBooks: LiveData<List<Student>>

    // Transactions
    val allTransactions: LiveData<List<Transaction>>
    val activeBorrowCount: LiveData<Int>

    // ─── INITIALIZATION (Must happen before Search/Filter variables) ─────────
    init {
        val db = AppDatabase.getDatabase(application)
        repository = LibraryRepository(
            db.bookDao(),
            db.studentDao(),
            db.transactionDao(),
            db.reviewDao()
        )
        allBooks = repository.allBooks
        allCategories = repository.allCategories
        totalBookCount = repository.totalBookCount
        allStudents = repository.allStudents
        topReadersByPages = repository.getTopReadersByPages(10)
        topReadersByBooks = repository.getTopReadersByBooks(10)
        allTransactions = repository.allTransactions
        activeBorrowCount = repository.activeBorrowCount

        // Mark overdue books on startup
        viewModelScope.launch { repository.markOverdue() }
    }

    // ─── FILTERS & STATUS (Safe to declare now that repository exists) ───────

    // Search
    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Book>> = _searchQuery.switchMap { query ->
        if (query.isBlank()) repository.allBooks
        else repository.searchBooks(query)
    }

    // Category filter
    private val _selectedCategory = MutableLiveData<String?>(null)
    val filteredBooks: LiveData<List<Book>> = _selectedCategory.switchMap { category ->
        if (category == null) repository.allBooks
        else repository.getBooksByCategory(category)
    }

    // Status messages
    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    // ─── Book Actions ─────────────────────────────────────────────────────────

    fun addBook(book: Book) = viewModelScope.launch {
        repository.insertBook(book)
        _statusMessage.value = "'${book.title}' ಪುಸ್ತಕ ಸೇರಿಸಲಾಗಿದೆ"
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        repository.updateBook(book)
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.deleteBook(book)
        _statusMessage.value = "'${book.title}' ಅಳಿಸಲಾಗಿದೆ"
    }

    fun getBookById(bookId: Long): LiveData<Book?> = repository.getBookById(bookId)

    suspend fun getBookByQrCode(qrCode: String): Book? = repository.getBookByQrCode(qrCode)

    suspend fun getBookByIsbn(isbn: String): Book? = repository.getBookByIsbn(isbn)

    fun searchBooks(query: String) { _searchQuery.value = query }

    fun filterByCategory(category: String?) { _selectedCategory.value = category }

    // ─── Student Actions ──────────────────────────────────────────────────────

    fun addStudent(student: Student) = viewModelScope.launch {
        repository.insertStudent(student)
        _statusMessage.value = "'${student.name}' ವಿದ್ಯಾರ್ಥಿ ನೋಂದಾಯಿಸಲಾಗಿದೆ"
    }

    fun getStudentById(studentId: Long): LiveData<Student?> = repository.getStudentById(studentId)

    fun searchStudents(query: String): LiveData<List<Student>> = repository.searchStudents(query)

    // ─── Transaction Actions ──────────────────────────────────────────────────

    fun issueBook(bookId: Long, studentId: Long, qrCode: String = "") = viewModelScope.launch {
        val transaction = Transaction(
            bookId = bookId,
            studentId = studentId,
            qrScanCode = qrCode
        )
        repository.issueBook(transaction)
        _statusMessage.value = "ಪುಸ್ತಕ ನೀಡಲಾಗಿದೆ"
    }

    fun returnBook(transactionId: Long, studentId: Long, pagesRead: Int) = viewModelScope.launch {
        repository.returnBook(transactionId, System.currentTimeMillis(), pagesRead, studentId, pagesRead)
        _statusMessage.value = "ಪುಸ್ತಕ ಹಿಂತಿರುಗಿಸಲಾಗಿದೆ"
    }

    fun getTransactionsByStudent(studentId: Long): LiveData<List<Transaction>> =
        repository.getTransactionsByStudent(studentId)

    fun getActiveTransactionsByStudent(studentId: Long): LiveData<List<Transaction>> =
        repository.getActiveTransactionsByStudent(studentId)

    fun getTransactionsByStatus(status: String): LiveData<List<Transaction>> =
        repository.getTransactionsByStatus(status)

    suspend fun getActiveTransactionByBook(bookId: Long): Transaction? =
        repository.getActiveTransactionByBook(bookId)

    // ─── Review Actions ───────────────────────────────────────────────────────

    fun addReview(bookId: Long, studentId: Long, rating: Float, reviewText: String) =
        viewModelScope.launch {
            val existing = repository.getReviewByBookAndStudent(bookId, studentId)
            if (existing != null) {
                repository.insertReview(existing.copy(rating = rating, reviewText = reviewText))
            } else {
                repository.insertReview(Review(bookId = bookId, studentId = studentId, rating = rating, reviewText = reviewText))
            }
            _statusMessage.value = "ನಿಮ್ಮ ವಿಮರ್ಶೆ ಸೇರಿಸಲಾಗಿದೆ"
        }

    fun getReviewsByBook(bookId: Long) = repository.getReviewsByBook(bookId)

    fun getAverageRatingForBook(bookId: Long) = repository.getAverageRatingForBook(bookId)

    fun getReviewCountForBook(bookId: Long) = repository.getReviewCountForBook(bookId)

    fun clearStatusMessage() { _statusMessage.value = null }
}

class LibraryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LibraryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}