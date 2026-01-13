package com.bramish.wheelofpasiva.presentation.game

import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.WheelSlice

/**
 * UI state for the Game screen.
 */
sealed class GameUiState {
    data object Loading : GameUiState()

    /**
     * Playing state - game is active.
     *
     * @property roomId The room ID
     * @property players List of players in the room
     * @property isSpinning Whether the wheel is currently spinning
     * @property currentTurnPlayer The player whose turn it is to spin
     * @property currentTurnIndex The index of the current turn player
     * @property isMyTurn Whether it's the current user's turn
     * @property isHost Whether the current player is the host
     * @property secretWord The secret word (visible to host, masked for others)
     * @property revealedLetters Set of letters that have been correctly guessed
     * @property playerScores Map of player ID to their score
     * @property myScore Current player's score
     * @property lastSliceIndex Index of the slice from last spin (0-7)
     * @property lastSliceResult The wheel slice result from last spin
     * @property hasExtraTurn Whether current player has an extra turn
     * @property showSecretWordDialog Whether to show dialog to set secret word
     * @property showGuessInput Whether to show the letter/word guess input
     * @property isGameOver Whether the game has ended
     * @property winnerId ID of the player who won
     * @property winnerName Name of the player who won
     */
    data class Playing(
        val roomId: String,
        val players: List<Player>,
        val isSpinning: Boolean = false,
        val currentTurnPlayer: Player? = null,
        val currentTurnIndex: Int = 0,
        val isMyTurn: Boolean = false,
        val isHost: Boolean = false,
        val secretWord: String? = null,
        val language: com.bramish.wheelofpasiva.domain.model.Language = com.bramish.wheelofpasiva.domain.model.Language.ENGLISH,
        val revealedLetters: Set<Char> = emptySet(),
        val playerScores: Map<String, Int> = emptyMap(),
        val myScore: Int = 0,
        val lastSliceIndex: Int? = null,
        val lastSliceResult: WheelSlice? = null,
        val hasExtraTurn: Boolean = false,
        val showSecretWordDialog: Boolean = false,
        val showGuessInput: Boolean = false,
        val isGameOver: Boolean = false,
        val winnerId: String? = null,
        val winnerName: String? = null
    ) : GameUiState() {
        /**
         * Gets the display text for the secret word.
         * Shows actual letters for revealed ones, underscores for hidden ones.
         * Spaces are always shown as spaces.
         */
        fun getDisplayWord(): String {
            val word = secretWord ?: return ""
            return word.map { char ->
                when {
                    char.isWhitespace() -> ' '
                    char.uppercaseChar() in revealedLetters -> char
                    isHost -> char // Host always sees the word
                    else -> '_'
                }
            }.joinToString(" ")
        }

        /**
         * Checks if the word has been completely revealed.
         */
        fun isWordFullyRevealed(): Boolean {
            val word = secretWord ?: return false
            return word.filter { !it.isWhitespace() }
                .all { it.uppercaseChar() in revealedLetters }
        }
    }

    data class Error(val message: String) : GameUiState()
}
