package com.example.nammapustaka1.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nammapustaka1.data.dao.*
import com.example.nammapustaka1.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Book::class, Student::class, Transaction::class, Review::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun studentDao(): StudentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun reviewDao(): ReviewDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "namma_pustaka_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        suspend fun seedDatabase(database: AppDatabase) {
            val bookDao = database.bookDao()
            val studentDao = database.studentDao()

            // Seed sample students
            studentDao.insertStudent(Student(name = "Ravi Kumar", rollNumber = "001", className = "Class 6", section = "A"))
            studentDao.insertStudent(Student(name = "Priya Devi", rollNumber = "002", className = "Class 7", section = "B"))
            studentDao.insertStudent(Student(name = "Suresh Naik", rollNumber = "003", className = "Class 6", section = "A"))
            studentDao.insertStudent(Student(name = "Kavya Rao", rollNumber = "004", className = "Class 8", section = "C"))
            studentDao.insertStudent(Student(name = "Arjun Gowda", rollNumber = "005", className = "Class 7", section = "A"))

            // Seed sample books
            bookDao.insertBook(
                Book(
                    title = "Malgudi Days",
                    author = "R.K. Narayan",
                    category = "Story",
                    isbn = "9780143065210",
                    totalPages = 268,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/9780143065210-M.jpg",
                    description = "ಮಾಲ್ಗುಡಿ ಊರಿನ ಮಕ್ಕಳ ಮತ್ತು ಜನರ ಜೀವನದ ಕಥೆಗಳ ಸಂಗ್ರಹ.",
                    qrCode = "BOOK_001",
                    availableCopies = 2,
                    totalCopies = 2
                )
            )
            bookDao.insertBook(
                Book(
                    title = "Wings of Fire",
                    author = "A.P.J. Abdul Kalam",
                    category = "Biography",
                    isbn = "9788173711466",
                    totalPages = 196,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/9788173711466-M.jpg",
                    description = "ಭಾರತದ ಮಿಸೈಲ್ ಮನುಷ್ಯ ಎ.ಪಿ.ಜೆ. ಅಬ್ದುಲ್ ಕಲಾಮ್ ಅವರ ಆತ್ಮಕಥೆ.",
                    qrCode = "BOOK_002",
                    availableCopies = 1,
                    totalCopies = 1
                )
            )
            bookDao.insertBook(
                Book(
                    title = "Science for Children",
                    author = "NCERT",
                    category = "Science",
                    isbn = "9788174504241",
                    totalPages = 320,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/9788174504241-M.jpg",
                    description = "ವಿಜ್ಞಾನದ ಮೂಲ ಪರಿಕಲ್ಪನೆಗಳನ್ನು ಮಕ್ಕಳಿಗಾಗಿ ಸರಳ ಭಾಷೆಯಲ್ಲಿ ವಿವರಿಸಲಾಗಿದೆ.",
                    qrCode = "BOOK_003",
                    availableCopies = 3,
                    totalCopies = 3
                )
            )
            bookDao.insertBook(
                Book(
                    title = "India: A History",
                    author = "John Keay",
                    category = "History",
                    isbn = "9780802137975",
                    totalPages = 576,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/9780802137975-M.jpg",
                    description = "ಭಾರತದ ಸಮಗ್ರ ಇತಿಹಾಸವನ್ನು ಆಕರ್ಷಕ ಶೈಲಿಯಲ್ಲಿ ನಿರೂಪಿಸಲಾಗಿದೆ.",
                    qrCode = "BOOK_004",
                    availableCopies = 1,
                    totalCopies = 2
                )
            )
            bookDao.insertBook(
                Book(
                    title = "Panchatantra",
                    author = "Vishnu Sharma",
                    category = "Story",
                    isbn = "9780143428220",
                    totalPages = 240,
                    coverUrl = "https://covers.openlibrary.org/b/isbn/9780143428220-M.jpg",
                    description = "ಪ್ರಾಣಿಗಳ ಕಥೆಗಳ ಮೂಲಕ ಜೀವನ ಮೌಲ್ಯಗಳನ್ನು ಕಲಿಸುವ ಪ್ರಾಚೀನ ಕಥಾಸಂಗ್ರಹ.",
                    qrCode = "BOOK_005",
                    availableCopies = 2,
                    totalCopies = 2
                )
            )
        }
    }
}
