package com.example.nammapustaka1.ui.addbbook

import android.Manifest
import android.R
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.nammapustaka1.data.model.Book
import com.example.nammapustaka1.databinding.ActivityAddBookBinding
import com.example.nammapustaka1.utils.GeminiHelper
import com.example.nammapustaka1.utils.OpenLibraryHelper
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private val viewModel: LibraryViewModel by viewModels {
        LibraryViewModelFactory(application)
    }

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var imageCapture: ImageCapture? = null
    private var capturedTitle: String = ""
    private var capturedAuthor: String = ""

    private val categories = listOf("Story", "Science", "History", "Biography", "Mathematics", "Nature", "Religion", "Other")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, "ಕ್ಯಾಮೆರಾ ಅನುಮತಿ ಅಗತ್ಯ", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "ಹೊಸ ಪುಸ್ತಕ ಸೇರಿಸಿ"

        setupCategorySpinner()
        setupButtons()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                binding.btnCapture.isEnabled = true
            } catch (e: Exception) {
                Log.e("AddBook", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupButtons() {
        // Capture button — takes photo and uses ML Kit text recognition + Gemini
        binding.btnCapture.setOnClickListener {
            captureAndAnalyze()
        }

        // Manual entry toggle
        binding.btnManualEntry.setOnClickListener {
            if (binding.layoutManualEntry.visibility == View.VISIBLE) {
                binding.layoutManualEntry.visibility = View.GONE
                binding.btnManualEntry.text = "ಹಸ್ತಚಾಲಿತ ನಮೂದು"
            } else {
                binding.layoutManualEntry.visibility = View.VISIBLE
                binding.btnManualEntry.text = "ಕ್ಯಾಮೆರಾ ಬಳಸಿ"
            }
        }

        // Save button
        binding.btnSaveBook.setOnClickListener {
            saveBook()
        }

        // Fetch Gemini summary
        binding.btnFetchSummary.setOnClickListener {
            fetchGeminiSummary()
        }
    }

    private fun captureAndAnalyze() {
        val capture = imageCapture ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "ಚಿತ್ರ ತೆಗೆಯಲಾಗುತ್ತಿದೆ..."

        capture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val mediaImage = imageProxy.image ?: run {
                    imageProxy.close()
                    binding.progressBar.visibility = View.GONE
                    return
                }

                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val rawText = visionText.text
                        imageProxy.close()
                        binding.tvStatus.text = "ಪಠ್ಯ ಗುರುತಿಸಲಾಗಿದೆ. Gemini ವಿಶ್ಲೇಷಿಸುತ್ತಿದೆ..."

                        lifecycleScope.launch {
                            val result = GeminiHelper.extractBookInfoFromText(rawText)
                            if (result != null) {
                                binding.etTitle.setText(result.first)
                                binding.etAuthor.setText(result.second)
                                capturedTitle = result.first
                                capturedAuthor = result.second
                                binding.layoutManualEntry.visibility = View.VISIBLE
                                binding.tvStatus.text = "ಮಾಹಿತಿ ಪಡೆಯಲಾಗಿದೆ. ಸರಿಪಡಿಸಿ ಮತ್ತು ಉಳಿಸಿ."
                                fetchGeminiSummary()
                            } else {
                                binding.tvStatus.text = "ಮಾಹಿತಿ ಪಡೆಯಲಾಗಲಿಲ್ಲ. ಹಸ್ತಚಾಲಿತ ನಮೂದು ಬಳಸಿ."
                                binding.layoutManualEntry.visibility = View.VISIBLE
                            }
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                    .addOnFailureListener {
                        imageProxy.close()
                        binding.progressBar.visibility = View.GONE
                        binding.tvStatus.text = "ಪಠ್ಯ ಗುರುತಿಸಲಾಗಲಿಲ್ಲ. ಹಸ್ತಚಾಲಿತ ನಮೂದು ಬಳಸಿ."
                        binding.layoutManualEntry.visibility = View.VISIBLE
                    }
            }

            override fun onError(exception: ImageCaptureException) {
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = "ಚಿತ್ರ ತೆಗೆಯಲು ವಿಫಲವಾಗಿದೆ"
            }
        })
    }

    private fun fetchGeminiSummary() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        if (title.isBlank()) { Toast.makeText(this, "ಮೊದಲು ಶೀರ್ಷಿಕೆ ನಮೂದಿಸಿ", Toast.LENGTH_SHORT).show(); return }

        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Gemini ಕನ್ನಡ ಸಾರಾಂಶ ತಯಾರಿಸುತ್ತಿದೆ..."

        lifecycleScope.launch {
            val summary = GeminiHelper.generateKannadaSummary(title, author)
            binding.etDescription.setText(summary ?: "ಸಾರಾಂಶ ಪಡೆಯಲಾಗಲಿಲ್ಲ")
            binding.progressBar.visibility = View.GONE
            binding.tvStatus.text = "ಸಾರಾಂಶ ಸಿದ್ಧ"
        }
    }

    private fun saveBook() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val isbn = binding.etIsbn.text.toString().trim()
        val pages = binding.etPages.text.toString().toIntOrNull() ?: 0
        val copies = binding.etCopies.text.toString().toIntOrNull() ?: 1
        val category = categories[binding.spinnerCategory.selectedItemPosition]
        val description = binding.etDescription.text.toString().trim()

        if (title.isBlank()) { binding.etTitle.error = "ಶೀರ್ಷಿಕೆ ಅಗತ್ಯ"; return }
        if (author.isBlank()) { binding.etAuthor.error = "ಲೇಖಕ ಅಗತ್ಯ"; return }

        val coverUrl = if (isbn.isNotBlank()) OpenLibraryHelper.getCoverUrlByIsbn(isbn) else ""
        val qrCode = OpenLibraryHelper.generateQrCode(title, isbn)

        val book = Book(
            title = title,
            author = author,
            isbn = isbn,
            totalPages = pages,
            category = category,
            description = description,
            coverUrl = coverUrl,
            qrCode = qrCode,
            totalCopies = copies,
            availableCopies = copies
        )

        viewModel.addBook(book)
        Toast.makeText(this, "'$title' ಸೇರಿಸಲಾಗಿದೆ!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
