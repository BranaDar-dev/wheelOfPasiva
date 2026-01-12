package com.bramish.wheelofpasiva.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a game room.
 *
 * @property id 6-digit unique room identifier
 * @property createdAt Timestamp when the room was created
 * @property hostId ID of the player who created the room
 * @property players List of players currently in the room (ordered by join time)
 * @property isGameStarted Whether the game has been started by the host
 * @property currentTurnIndex Index of the player whose turn it is (0-based, into players list)
 * @property isSpinning Whether the wheel is currently spinning
 * @property secretWord The secret word/phrase set by the host (only visible to host)
 * @property playerScores Map of player ID to their accumulated score
 * @property revealedLetters String of letters that have been correctly guessed
 * @property lastSliceIndex Index of the slice from the last spin (0-7)
 * @property hasExtraTurn Whether current player has an extra turn
 * @property isGameOver Whether the word has been guessed and game is over
 * @property winnerId ID of the player who guessed the word (if game is over)
 */
data class Room(
    val id: String,
    val createdAt: Instant,
    val hostId: String,
    val players: List<Player>,
    val isGameStarted: Boolean = false,
    val currentTurnIndex: Int = 0,
    val isSpinning: Boolean = false,
    val secretWord: String? = null,
    val playerScores: Map<String, Int> = emptyMap(),
    val revealedLetters: String = "",
    val lastSliceIndex: Int? = null,
    val hasExtraTurn: Boolean = false,
    val isGameOver: Boolean = false,
    val winnerId: String? = null
)
