package com.example.nammapustaka1.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nammapustaka1.R
import com.example.nammapustaka1.adapter.TransactionAdapter
import com.example.nammapustaka1.data.model.Student
import com.example.nammapustaka1.data.model.TransactionStatus
import com.example.nammapustaka1.databinding.ActivityBookDetailBinding
import com.example.nammapustaka1.ui.qrscan.QRScanActivity
import com.example.nammapustaka1.utils.DateUtils
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class BookDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
        const val REQUEST_QR_SCAN = 101
    }

    private lateinit var binding: ActivityBookDetailBinding
    private val viewModel: LibraryViewModel by viewModels {
        LibraryViewModelFactory(application)
    }

    private var bookId: Long = -1
    private var studentList: List<Student> = emptyList()
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        bookId = intent.getLongExtra(EXTRA_BOOK_ID, -1)
        if (bookId == -1L) { finish(); return }

        setupTransactionList()
        observeBook()
        observeStudents()
        observeReviews()

        binding.btnIssueBook.setOnClickListener { showIssueBookDialog() }
        binding.btnQrScan.setOnClickListener {
            startActivityForResult(
                Intent(this, QRScanActivity::class.java),
                REQUEST_QR_SCAN
            )
        }
        binding.btnAddReview.setOnClickListener { showReviewDialog() }
    }

    private fun setupTransactionList() {
        transactionAdapter = TransactionAdapter(
            onReturnClick = { tx ->
                showReturnDialog(tx.id, tx.studentId)
            }
        )
        binding.rvTransactions.adapter = transactionAdapter
    }

    private fun observeBook() {
        viewModel.getBookById(bookId).observe(this) { book ->
            book ?: return@observe
            supportActionBar?.title = book.title
            binding.tvBookTitle.text = book.title
            binding.tvAuthor.text = "ಲೇಖಕ: ${book.author}"
            binding.tvCategory.text = "ವಿಭಾಗ: ${book.category}"
            binding.tvPages.text = "ಪುಟಗಳು: ${book.totalPages}"
            binding.tvAddedDate.text = "ಸೇರಿಸಿದ ದಿನ: ${DateUtils.formatDate(book.addedDate)}"
            binding.tvDescription.text = book.description.ifBlank { "ಸಾರಾಂಶ ಲಭ್ಯವಿಲ್ಲ" }
            binding.tvQrCode.text = "QR: ${book.qrCode}"

            // Availability
            if (book.availableCopies > 0) {
                binding.tvAvailability.text = "ಲಭ್ಯ: ${book.availableCopies}/${book.totalCopies} ಪ್ರತಿಗಳು"
                binding.tvAvailability.setTextColor(Color.parseColor("#2E7D32"))
                binding.btnIssueBook.isEnabled = true
            } else {
                binding.tvAvailability.text = "ಲಭ್ಯವಿಲ್ಲ (ಎಲ್ಲಾ ಪ್ರತಿಗಳು ನೀಡಲಾಗಿದೆ)"
                binding.tvAvailability.setTextColor(Color.RED)
                binding.btnIssueBook.isEnabled = false
            }

            // Cover
            if (book.coverUrl.isNotBlank()) {
                Glide.with(this).load(book.coverUrl)
                    .placeholder(R.drawable.ic_book_placeholder)
                    .error(R.drawable.ic_book_placeholder)
                    .into(binding.ivBookCover)
            }
        }

        // Transactions for this book
        viewModel.getTransactionsByStudent(bookId).observe(this) { /* observe via book transactions */ }
    }

    private fun observeStudents() {
        viewModel.allStudents.observe(this) { students ->
            studentList = students
        }
    }

    private fun observeReviews() {
        viewModel.getAverageRatingForBook(bookId).observe(this) { avg ->
            val rating = avg ?: 0f
            binding.ratingBar.rating = rating
            binding.tvRatingText.text = String.format("%.1f / 5.0", rating)
        }
        viewModel.getReviewCountForBook(bookId).observe(this) { count ->
            binding.tvReviewCount.text = "($count ವಿಮರ್ಶೆಗಳು)"
        }
    }

    private fun showIssueBookDialog() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "ಮೊದಲು ವಿದ್ಯಾರ್ಥಿಗಳನ್ನು ಸೇರಿಸಿ", Toast.LENGTH_SHORT).show()
            return
        }

        val studentNames = studentList.map { "${it.name} (${it.rollNumber})" }.toTypedArray()
        var selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("ಯಾವ ವಿದ್ಯಾರ್ಥಿಗೆ ನೀಡಬೇಕು?")
            .setSingleChoiceItems(studentNames, 0) { _, which -> selectedIndex = which }
            .setPositiveButton("ನೀಡಿ") { _, _ ->
                val student = studentList[selectedIndex]
                viewModel.issueBook(bookId, student.id)
                Toast.makeText(this, "${student.name} ಗೆ ಪುಸ್ತಕ ನೀಡಲಾಗಿದೆ", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("ರದ್ದು", null)
            .show()
    }

    private fun showReturnDialog(transactionId: Long, studentId: Long) {
        val book = viewModel.getBookById(bookId).value
        val pagesInput = android.widget.EditText(this).apply {
            hint = "ಓದಿದ ಪುಟಗಳು (ಐಚ್ಛಿಕ)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        AlertDialog.Builder(this)
            .setTitle("ಪುಸ್ತಕ ಹಿಂತಿರುಗಿಸು")
            .setMessage("ಈ ಪುಸ್ತಕ ಹಿಂತಿರುಗಿಸಬೇಕೇ?")
            .setView(pagesInput)
            .setPositiveButton("ಹಿಂತಿರುಗಿಸಿ") { _, _ ->
                val pages = pagesInput.text.toString().toIntOrNull() ?: (book?.totalPages ?: 0)
                viewModel.returnBook(transactionId, studentId, pages)
            }
            .setNegativeButton("ರದ್ದು", null)
            .show()
    }

    private fun showReviewDialog() {
        if (studentList.isEmpty()) {
            Toast.makeText(this, "ಮೊದಲು ವಿದ್ಯಾರ್ಥಿಗಳನ್ನು ಸೇರಿಸಿ", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_review, null)
        val spinnerStudent = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerStudent)
        val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBarReview)
        val etReview = dialogView.findViewById<android.widget.EditText>(R.id.etReviewText)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            studentList.map { "${it.name} (${it.rollNumber})" })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStudent.adapter = adapter

        AlertDialog.Builder(this)
            .setTitle("ವಿಮರ್ಶೆ ಬರೆಯಿರಿ")
            .setView(dialogView)
            .setPositiveButton("ಸಲ್ಲಿಸಿ") { _, _ ->
                val selectedStudent = studentList[spinnerStudent.selectedItemPosition]
                val rating = ratingBar.rating
                val reviewText = etReview.text.toString().trim()
                if (rating == 0f) {
                    Toast.makeText(this, "ದಯವಿಟ್ಟು ರೇಟಿಂಗ್ ನೀಡಿ", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addReview(bookId, selectedStudent.id, rating, reviewText)
            }
            .setNegativeButton("ರದ್ದು", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_QR_SCAN && resultCode == RESULT_OK) {
            val scannedCode = data?.getStringExtra(QRScanActivity.EXTRA_QR_RESULT) ?: return
            lifecycleScope.launch {
                val book = viewModel.getBookByQrCode(scannedCode)
                if (book != null && book.id == bookId) {
                    showIssueBookDialog()
                } else {
                    Toast.makeText(this@BookDetailActivity, "QR ಕೋಡ್ ಈ ಪುಸ್ತಕಕ್ಕೆ ಹೊಂದಿಕೆಯಾಗುವುದಿಲ್ಲ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }
}
