package com.bramish.wheelofpasiva.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.usecase.ObserveRoomUseCase
import com.bramish.wheelofpasiva.domain.usecase.StartGameUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Room screen.
 * Observes real-time room updates and displays room ID and player list.
 * Handles game start functionality (host-only).
 */
class RoomViewModel(
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val startGameUseCase: StartGameUseCase,
    private val roomId: String,
    private val playerId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoomUiState>(RoomUiState.Loading)
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        observeRoom()
    }

    /**
     * Starts observing the room for real-time updates.
     */
    private fun observeRoom() {
        viewModelScope.launch {
            observeRoomUseCase(roomId)
                .catch { error ->
                    _uiState.value = RoomUiState.Error(
                        error.message ?: "Failed to load room. Please try again."
                    )
                }
                .collect { result ->
                    result
                        .onSuccess { room ->
                            // Check if game has started - navigate all players to game screen
                            if (room.isGameStarted) {
                                _navigationEvent.emit(NavigationEvent.NavigateToGame)
                            }

                            _uiState.value = RoomUiState.Success(
                                room = room,
                                players = room.players,
                                isCurrentPlayerHost = room.hostId == playerId
                            )
                        }
                        .onFailure { error ->
                            val errorMessage = when (error) {
                                is RoomError.RoomNotFound -> "Room not found"
                                is RoomError.NetworkError -> "Connection error. Please check your internet."
                                else -> error.message ?: "Failed to load room"
                            }
                            _uiState.value = RoomUiState.Error(errorMessage)
                        }
                }
        }
    }

    /**
     * Starts the game. Only the host can start the game.
     */
    fun startGame() {
        val state = _uiState.value
        if (state !is RoomUiState.Success || !state.isCurrentPlayerHost) {
            return
        }

        viewModelScope.launch {
            startGameUseCase(roomId)
                .onFailure { error ->
                    // Could show an error snackbar here if needed
                }
        }
    }

    /**
     * Retries loading the room (used after errors).
     */
    fun retry() {
        _uiState.value = RoomUiState.Loading
        observeRoom()
    }

    /**
     * Gets the room ID for display.
     */
    fun getRoomId(): String = roomId

    /**
     * Checks if the given player is the host.
     */
    fun isHost(player: Player, room: Room): Boolean {
        return player.id == room.hostId
    }

    /**
     * Navigation events for the Room screen.
     */
    sealed class NavigationEvent {
        data object NavigateToGame : NavigationEvent()
    }
}
