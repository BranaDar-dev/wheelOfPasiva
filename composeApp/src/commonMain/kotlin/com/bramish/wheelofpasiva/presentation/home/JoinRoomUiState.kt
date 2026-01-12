package com.bramish.wheelofpasiva.presentation.home

/**
 * UI state for the join room dialog.
 */
sealed class JoinRoomUiState {
    /**
     * Idle state - dialog open, ready for input.
     */
    data object Idle : JoinRoomUiState()

    /**
     * Loading state - validating room and joining.
     */
    data object Loading : JoinRoomUiState()

    /**
     * Success state - room joined successfully.
     *
     * @property roomId The ID of the joined room
     */
    data class Success(val roomId: String) : JoinRoomUiState()

    /**
     * Error state - join failed, keep dialog open.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : JoinRoomUiState()
}
