package com.bramish.wheelofpasiva.scanner

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android implementation of QR code scanner using CameraX and ML Kit.
 */
actual class QrCodeScanner(private val context: Context) {

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val barcodeScanner = BarcodeScanning.getClient()

    /**
     * Scans a QR code using CameraX and ML Kit.
     */
    actual suspend fun scanQrCode(): Result<String?> = suspendCancellableCoroutine { continuation ->
        if (!hasPermission()) {
            continuation.resumeWithException(
                PermissionDeniedException("Camera permission not granted")
            )
            return@suspendCancellableCoroutine
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                var isScanned = false

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (isScanned) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    @androidx.camera.core.ExperimentalGetImage
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        barcodeScanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                                        isScanned = true
                                        val qrContent = barcode.rawValue
                                        cameraProvider.unbindAll()
                                        continuation.resume(Result.success(qrContent))
                                        break
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                if (!isScanned) {
                                    isScanned = true
                                    cameraProvider.unbindAll()
                                    continuation.resume(Result.failure(exception))
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as ComponentActivity,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }

            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))

        continuation.invokeOnCancellation {
            cameraExecutor.shutdown()
        }
    }

    /**
     * Checks if camera permission is granted.
     */
    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests camera permission.
     */
    actual suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        if (hasPermission()) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        val activity = context as? ComponentActivity
        if (activity == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }

        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            continuation.resume(isGranted)
        }

        launcher.launch(Manifest.permission.CAMERA)
    }
}
