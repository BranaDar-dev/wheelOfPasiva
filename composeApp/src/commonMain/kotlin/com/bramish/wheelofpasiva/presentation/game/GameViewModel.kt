package com.bramish.wheelofpasiva.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.usecase.ObserveRoomUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel for the Game screen.
 * Manages the wheel spinning logic and player selection.
 */
class GameViewModel(
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val roomId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var currentPlayers: List<Player> = emptyList()

    init {
        observeRoom()
    }

    private fun observeRoom() {
        viewModelScope.launch {
            observeRoomUseCase(roomId)
                .catch { error ->
                    _uiState.value = GameUiState.Error(
                        error.message ?: "Failed to load game. Please try again."
                    )
                }
                .collect { result ->
                    result
                        .onSuccess { room ->
                            currentPlayers = room.players
                            val currentState = _uiState.value
                            _uiState.value = GameUiState.Playing(
                                roomId = roomId,
                                players = room.players,
                                isSpinning = (currentState as? GameUiState.Playing)?.isSpinning ?: false,
                                selectedPlayer = (currentState as? GameUiState.Playing)?.selectedPlayer
                            )
                        }
                        .onFailure { error ->
                            val errorMessage = when (error) {
                                is RoomError.RoomNotFound -> "Room not found"
                                is RoomError.NetworkError -> "Connection error. Please check your internet."
                                else -> error.message ?: "Failed to load game"
                            }
                            _uiState.value = GameUiState.Error(errorMessage)
                        }
                }
        }
    }

    /**
     * Spins the wheel to randomly select a player.
     */
    fun spinWheel() {
        val state = _uiState.value
        if (state !is GameUiState.Playing || state.isSpinning || currentPlayers.isEmpty()) {
            return
        }

        viewModelScope.launch {
            // Start spinning
            _uiState.value = state.copy(isSpinning = true, selectedPlayer = null)

            // Simulate spinning animation delay
            delay(2000)

            // Select random player
            val selectedPlayer = currentPlayers[Random.nextInt(currentPlayers.size)]

            // Stop spinning and show result
            _uiState.value = state.copy(
                isSpinning = false,
                selectedPlayer = selectedPlayer
            )
        }
    }

    /**
     * Resets the wheel for another spin.
     */
    fun resetWheel() {
        val state = _uiState.value
        if (state is GameUiState.Playing) {
            _uiState.value = state.copy(selectedPlayer = null)
        }
    }

    /**
     * Retries loading the game (used after errors).
     */
    fun retry() {
        _uiState.value = GameUiState.Loading
        observeRoom()
    }

    fun getRoomId(): String = roomId
}
