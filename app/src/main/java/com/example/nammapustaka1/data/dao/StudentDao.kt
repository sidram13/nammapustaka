package com.example.nammapustaka1.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.nammapustaka1.data.model.Student

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): LiveData<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: Long): LiveData<Student?>

    @Query("SELECT * FROM students WHERE rollNumber = :rollNumber LIMIT 1")
    suspend fun getStudentByRollNumber(rollNumber: String): Student?

    @Query("SELECT * FROM students ORDER BY totalPagesRead DESC LIMIT :limit")
    fun getTopReadersByPages(limit: Int = 10): LiveData<List<Student>>

    @Query("SELECT * FROM students ORDER BY booksRead DESC LIMIT :limit")
    fun getTopReadersByBooks(limit: Int = 10): LiveData<List<Student>>

    @Query("UPDATE students SET totalPagesRead = totalPagesRead + :pages, booksRead = booksRead + 1 WHERE id = :studentId")
    suspend fun updateReadingStats(studentId: Long, pages: Int)

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR rollNumber LIKE '%' || :query || '%'")
    fun searchStudents(query: String): LiveData<List<Student>>
}
