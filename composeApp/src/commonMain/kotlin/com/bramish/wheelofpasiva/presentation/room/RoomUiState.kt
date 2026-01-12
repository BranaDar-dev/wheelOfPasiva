package com.bramish.wheelofpasiva.presentation.room

import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room

/**
 * UI state for the room screen.
 */
sealed class RoomUiState {
    /**
     * Loading state - fetching room data.
     */
    data object Loading : RoomUiState()

    /**
     * Success state - room data loaded.
     *
     * @property room The room data
     * @property players List of players in the room
     */
    data class Success(
        val room: Room,
        val players: List<Player>
    ) : RoomUiState()

    /**
     * Error state - failed to load room.
     *
     * @property message Error message to display
     */
    data class Error(val message: String) : RoomUiState()
}
