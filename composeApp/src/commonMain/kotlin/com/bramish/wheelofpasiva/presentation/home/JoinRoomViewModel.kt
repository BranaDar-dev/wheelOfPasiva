package com.bramish.wheelofpasiva.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.usecase.JoinRoomUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Join Room dialog.
 * Manages room ID input, validation, and joining logic.
 */
class JoinRoomViewModel(
    private val joinRoomUseCase: JoinRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<JoinRoomUiState>(JoinRoomUiState.Idle)
    val uiState: StateFlow<JoinRoomUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _roomId = MutableStateFlow("")
    val roomId: StateFlow<String> = _roomId.asStateFlow()

    /**
     * Updates the room ID input.
     * Only allows numeric input up to 6 digits.
     */
    fun onRoomIdChange(newRoomId: String) {
        // Only allow digits and max 6 characters
        if (newRoomId.all { it.isDigit() } && newRoomId.length <= 6) {
            _roomId.value = newRoomId
            // Clear error when user starts typing
            if (_uiState.value is JoinRoomUiState.Error) {
                _uiState.value = JoinRoomUiState.Idle
            }
        }
    }

    /**
     * Attempts to join the room with the given room ID and nickname.
     */
    fun onJoinRoom(nickname: String) {
        val currentRoomId = _roomId.value.trim()
        val currentNickname = nickname.trim()

        // Basic validation
        if (currentNickname.isBlank()) {
            _uiState.value = JoinRoomUiState.Error("Please enter a nickname")
            return
        }

        if (currentRoomId.isBlank()) {
            _uiState.value = JoinRoomUiState.Error("Please enter a room code")
            return
        }

        if (currentRoomId.length != 6) {
            _uiState.value = JoinRoomUiState.Error("Room code must be 6 digits")
            return
        }

        viewModelScope.launch {
            _uiState.value = JoinRoomUiState.Loading

            joinRoomUseCase(currentRoomId, currentNickname)
                .onSuccess { playerId ->
                    _uiState.value = JoinRoomUiState.Success(currentRoomId)
                    _navigationEvent.emit(NavigationEvent.NavigateToRoom(currentRoomId, playerId))
                }
                .onFailure { error ->
                    val errorMessage = when (error) {
                        is RoomError.RoomNotFound -> "Room does not exist. Please check the code."
                        is RoomError.InvalidRoomId -> "Room code must be 6 digits"
                        is RoomError.NetworkError -> "Connection error. Please try again."
                        is RoomError.PermissionDenied -> "Permission denied"
                        else -> error.message ?: "Failed to join room. Please try again."
                    }
                    _uiState.value = JoinRoomUiState.Error(errorMessage)
                }
        }
    }

    /**
     * Handles QR code scan result.
     */
    fun onQrCodeScanned(scannedCode: String) {
        // Extract room ID from QR code (might be a URL or just the ID)
        val extractedRoomId = extractRoomId(scannedCode)

        if (extractedRoomId != null && extractedRoomId.matches(Regex("^\\d{6}$"))) {
            _roomId.value = extractedRoomId
            // Auto-clear error if valid code is scanned
            if (_uiState.value is JoinRoomUiState.Error) {
                _uiState.value = JoinRoomUiState.Idle
            }
        } else {
            _uiState.value = JoinRoomUiState.Error("Invalid QR code. Please scan a valid room code.")
        }
    }

    /**
     * Opens the QR code scanner.
     */
    fun onOpenQrScanner() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.OpenQrScanner)
        }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        if (_uiState.value is JoinRoomUiState.Error) {
            _uiState.value = JoinRoomUiState.Idle
        }
    }

    /**
     * Resets the dialog state (used when dialog is dismissed).
     */
    fun reset() {
        _roomId.value = ""
        _uiState.value = JoinRoomUiState.Idle
    }

    /**
     * Extracts room ID from QR code content.
     * Handles both plain room IDs and URLs containing room IDs.
     */
    private fun extractRoomId(qrContent: String): String? {
        // If it's just digits, return as is
        if (qrContent.matches(Regex("^\\d{6}$"))) {
            return qrContent
        }

        // Try to extract from URL pattern (e.g., "https://example.com/room/123456")
        val urlPattern = Regex("\\d{6}")
        return urlPattern.find(qrContent)?.value
    }

    /**
     * Navigation events for the Join Room dialog.
     */
    sealed class NavigationEvent {
        data class NavigateToRoom(val roomId: String, val playerId: String) : NavigationEvent()
        data object OpenQrScanner : NavigationEvent()
    }
}
