package com.example.nammapustaka1.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nammapustaka1.data.model.Book

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: Long): LiveData<Book?>

    @Query("SELECT * FROM books WHERE category = :category ORDER BY title ASC")
    fun getBooksByCategory(category: String): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchBooks(query: String): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE qrCode = :qrCode LIMIT 1")
    suspend fun getBookByQrCode(qrCode: String): Book?

    @Query("UPDATE books SET availableCopies = availableCopies - 1 WHERE id = :bookId AND availableCopies > 0")
    suspend fun decrementAvailableCopies(bookId: Long)

    @Query("UPDATE books SET availableCopies = availableCopies + 1 WHERE id = :bookId AND availableCopies < totalCopies")
    suspend fun incrementAvailableCopies(bookId: Long)

    @Query("SELECT DISTINCT category FROM books ORDER BY category ASC")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT COUNT(*) FROM books")
    fun getTotalBookCount(): LiveData<Int>

    @Query("SELECT * FROM books WHERE isbn = :isbn LIMIT 1")
    suspend fun getBookByIsbn(isbn: String): Book?
}
