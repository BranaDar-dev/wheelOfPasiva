package com.bramish.wheelofpasiva.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.usecase.CreateRoomUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen.
 * Manages nickname input and room creation.
 */
class HomeViewModel(
    private val createRoomUseCase: CreateRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    /**
     * Updates the nickname input.
     */
    fun onNicknameChange(newNickname: String) {
        _nickname.value = newNickname
    }

    /**
     * Creates a new room with the current nickname as host.
     */
    fun onCreateRoom() {
        val currentNickname = _nickname.value.trim()
        if (currentNickname.isBlank()) {
            _uiState.value = HomeUiState.Error("Please enter a nickname")
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            createRoomUseCase(currentNickname)
                .onSuccess { result ->
                    _uiState.value = HomeUiState.Idle
                    _navigationEvent.emit(
                        NavigationEvent.NavigateToRoom(result.roomId, result.playerId)
                    )
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to create room. Please try again."
                    )
                }
        }
    }

    /**
     * Shows the join room dialog.
     */
    fun onJoinRoom() {
        val currentNickname = _nickname.value.trim()
        if (currentNickname.isBlank()) {
            _uiState.value = HomeUiState.Error("Please enter a nickname")
            return
        }

        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.ShowJoinDialog)
        }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        if (_uiState.value is HomeUiState.Error) {
            _uiState.value = HomeUiState.Idle
        }
    }

    /**
     * Navigation events for the Home screen.
     */
    sealed class NavigationEvent {
        data class NavigateToRoom(val roomId: String, val playerId: String) : NavigationEvent()
        data object ShowJoinDialog : NavigationEvent()
    }
}
