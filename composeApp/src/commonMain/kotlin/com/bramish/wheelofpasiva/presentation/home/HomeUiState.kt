package com.bramish.wheelofpasiva.presentation.home

/**
 * UI state for the home screen.
 */
sealed class HomeUiState {
    /**
     * Idle state - no operations in progress.
     */
    data object Idle : HomeUiState()

    /**
     * Loading state - operation in progress.
     */
    data object Loading : HomeUiState()

    /**
     * Error state - operation failed.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : HomeUiState()
}
