package com.bramish.wheelofpasiva.presentation.game

import com.bramish.wheelofpasiva.domain.model.Player

/**
 * UI state for the Game screen.
 */
sealed class GameUiState {
    data object Loading : GameUiState()

    data class Playing(
        val roomId: String,
        val players: List<Player>,
        val isSpinning: Boolean = false,
        val selectedPlayer: Player? = null
    ) : GameUiState()

    data class Error(val message: String) : GameUiState()
}
