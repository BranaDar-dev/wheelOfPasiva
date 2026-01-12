package com.bramish.wheelofpasiva.domain.model

/**
 * Sealed class representing domain-specific errors that can occur during room operations.
 */
sealed class RoomError : Exception() {
    /**
     * Error when a room with the specified ID does not exist.
     */
    data class RoomNotFound(val roomId: String) : RoomError() {
        override val message: String
            get() = "Room $roomId does not exist"
    }

    /**
     * Error when room ID generation fails after maximum retry attempts.
     */
    data class RoomIdGenerationFailed(override val cause: Throwable?) : RoomError() {
        override val message: String
            get() = "Failed to generate unique room ID"
    }

    /**
     * Error when a network operation fails.
     */
    data class NetworkError(override val cause: Throwable?) : RoomError() {
        override val message: String
            get() = "Network error: ${cause?.message}"
    }

    /**
     * Error when the provided room ID format is invalid.
     */
    data class InvalidRoomId(val roomId: String) : RoomError() {
        override val message: String
            get() = "Invalid room ID: must be 6 digits"
    }

    /**
     * Error when camera permission is denied.
     */
    data object PermissionDenied : RoomError() {
        override val message: String
            get() = "Camera permission required for QR scanning"
    }
}
