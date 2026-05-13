package com.example.nammapustaka1.ui.qrscan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.example.nammapustaka1.databinding.ActivityQrScanBinding
import com.example.nammapustaka1.ui.detail.BookDetailActivity
import com.example.nammapustaka1.viewmodel.LibraryViewModel
import com.example.nammapustaka1.viewmodel.LibraryViewModelFactory
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class QRScanActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QR_RESULT = "extra_qr_result"
        const val MODE_FIND_BOOK = "mode_find_book"  // Navigate to book detail
        const val MODE_RETURN_RESULT = "mode_return_result"  // Return scanned QR to caller
    }

    private lateinit var binding: ActivityQrScanBinding
    private val viewModel: LibraryViewModel by viewModels {
        LibraryViewModelFactory(application)
    }

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val scanProcessing = AtomicBoolean(false)
    private val mode: String get() = intent.getStringExtra("mode") ?: MODE_FIND_BOOK

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else Toast.makeText(this, "ಕ್ಯಾಮೆರಾ ಅನುಮತಿ ಅಗತ್ಯ", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "QR ಕೋಡ್ ಸ್ಕ್ಯಾನ್ ಮಾಡಿ"

        binding.tvInstruction.text = "ಪುಸ್ತಕದ QR ಕೋಡ್ ಮೇಲೆ ಕ್ಯಾಮೆರಾ ಇಡಿ"

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { qrValue ->
                        handleQrResult(qrValue)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("QRScan", "Camera bind failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun handleQrResult(qrValue: String) {
        if (!scanProcessing.compareAndSet(false, true)) return

        runOnUiThread {
            binding.tvScannedCode.text = "ಸ್ಕ್ಯಾನ್: $qrValue"
            binding.tvScannedCode.visibility = View.VISIBLE
        }

        when (mode) {
            MODE_RETURN_RESULT -> {
                val resultIntent = Intent()
                resultIntent.putExtra(EXTRA_QR_RESULT, qrValue)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            else -> {
                // Find the book with this QR code and open its detail page
                lifecycleScope.launch {
                    val book = viewModel.getBookByQrCode(qrValue)
                    if (book != null) {
                        val intent = Intent(this@QRScanActivity, BookDetailActivity::class.java)
                        intent.putExtra(BookDetailActivity.EXTRA_BOOK_ID, book.id)
                        startActivity(intent)
                        finish()
                    } else {
                        runOnUiThread {
                            binding.tvScannedCode.text = "ಪುಸ್ತಕ ಕಂಡುಬಂದಿಲ್ಲ: $qrValue"
                            Toast.makeText(this@QRScanActivity, "ಈ QR ಕೋಡ್‌ಗೆ ಪುಸ್ತಕ ಇಲ್ಲ", Toast.LENGTH_LONG).show()
                            scanProcessing.set(false)
                        }
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { onBackPressedDispatcher.onBackPressed(); return true }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // ─── ML Kit Barcode Analyzer ──────────────────────────────────────────────
    private class BarcodeAnalyzer(private val onQrDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        @androidx.camera.core.ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.format == Barcode.FORMAT_QR_CODE || barcode.format == Barcode.FORMAT_EAN_13 || barcode.format == Barcode.FORMAT_EAN_8) {
                            barcode.rawValue?.let { onQrDetected(it) }
                            break
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}
