package com.bramish.wheelofpasiva.scanner

/**
 * Platform-agnostic QR code scanner interface.
 * Provides camera-based QR code scanning functionality.
 */
expect class QrCodeScanner {
    /**
     * Scans a QR code using the device camera.
     * Suspends until a QR code is scanned or scanning is cancelled.
     *
     * @return The scanned QR code content, or null if scanning was cancelled
     * @throws PermissionDeniedException if camera permission is not granted
     */
    suspend fun scanQrCode(): Result<String?>

    /**
     * Checks if the app has camera permission.
     *
     * @return true if camera permission is granted, false otherwise
     */
    fun hasPermission(): Boolean

    /**
     * Requests camera permission from the user.
     *
     * @return true if permission was granted, false otherwise
     */
    suspend fun requestPermission(): Boolean
}

/**
 * Exception thrown when camera permission is denied.
 */
class PermissionDeniedException(message: String) : Exception(message)
