package com.bramish.wheelofpasiva.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.usecase.ObserveRoomUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the Room screen.
 * Observes real-time room updates and displays room ID and player list.
 */
class RoomViewModel(
    private val observeRoomUseCase: ObserveRoomUseCase,
    private val roomId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoomUiState>(RoomUiState.Loading)
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

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
                            _uiState.value = RoomUiState.Success(
                                room = room,
                                players = room.players
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
}
