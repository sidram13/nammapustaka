package com.example.nammapustaka1.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nammapustaka1.data.model.Review

@Dao
interface ReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review): Long

    @Update
    suspend fun updateReview(review: Review)

    @Delete
    suspend fun deleteReview(review: Review)

    @Query("SELECT * FROM reviews WHERE bookId = :bookId ORDER BY reviewDate DESC")
    fun getReviewsByBook(bookId: Long): LiveData<List<Review>>

    @Query("SELECT * FROM reviews WHERE studentId = :studentId ORDER BY reviewDate DESC")
    fun getReviewsByStudent(studentId: Long): LiveData<List<Review>>

    @Query("SELECT AVG(rating) FROM reviews WHERE bookId = :bookId")
    fun getAverageRatingForBook(bookId: Long): LiveData<Float?>

    @Query("SELECT COUNT(*) FROM reviews WHERE bookId = :bookId")
    fun getReviewCountForBook(bookId: Long): LiveData<Int>

    @Query("SELECT * FROM reviews WHERE bookId = :bookId AND studentId = :studentId LIMIT 1")
    suspend fun getReviewByBookAndStudent(bookId: Long, studentId: Long): Review?
}
