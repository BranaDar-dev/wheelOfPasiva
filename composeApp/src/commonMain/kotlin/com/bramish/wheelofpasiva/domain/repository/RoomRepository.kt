package com.bramish.wheelofpasiva.domain.repository

import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for room operations.
 * Follows the Dependency Inversion Principle - domain layer defines the contract,
 * data layer provides the implementation.
 */
interface RoomRepository {
    /**
     * Creates a new room in the database.
     *
     * @param room The room to create
     * @return Result indicating success or failure
     */
    suspend fun createRoom(room: Room): Result<Unit>

    /**
     * Checks if a room with the given ID exists.
     *
     * @param roomId The room ID to check
     * @return Result containing true if room exists, false otherwise
     */
    suspend fun roomExists(roomId: String): Result<Boolean>

    /**
     * Adds a player to an existing room.
     *
     * @param roomId The ID of the room to join
     * @param player The player to add
     * @return Result indicating success or failure
     */
    suspend fun addPlayer(roomId: String, player: Player): Result<Unit>

    /**
     * Observes real-time changes to a room.
     *
     * @param roomId The ID of the room to observe
     * @return Flow emitting room updates or errors
     */
    fun observeRoom(roomId: String): Flow<Result<Room>>

    /**
     * Gets a room by ID (one-time fetch).
     *
     * @param roomId The ID of the room to retrieve
     * @return Result containing the room or error
     */
    suspend fun getRoom(roomId: String): Result<Room>

    /**
     * Updates the game state with wheel game fields.
     *
     * @param roomId The ID of the room to update
     * @param isSpinning Whether the wheel is currently spinning
     * @param nextTurnIndex The index of the next player's turn
     * @param playerScores Map of player scores
     * @param revealedLetters String of revealed letters
     * @param lastSliceIndex Index of the last spin result
     * @param hasExtraTurn Whether current player has extra turn
     * @param isGameOver Whether the game is over
     * @param winnerId ID of the winner
     * @return Result indicating success or failure
     */
    suspend fun updateGameState(
        roomId: String,
        isSpinning: Boolean? = null,
        nextTurnIndex: Int? = null,
        playerScores: Map<String, Int>? = null,
        revealedLetters: String? = null,
        lastSliceIndex: Int? = null,
        hasExtraTurn: Boolean? = null,
        isGameOver: Boolean? = null,
        winnerId: String? = null
    ): Result<Unit>

    /**
     * Starts the game in a room.
     * Sets isGameStarted to true so all players navigate to the game screen.
     *
     * @param roomId The ID of the room to start the game in
     * @return Result indicating success or failure
     */
    suspend fun startGame(roomId: String): Result<Unit>

    /**
     * Sets the secret word/phrase for the game.
     * Only the host should call this.
     *
     * @param roomId The ID of the room
     * @param secretWord The secret word or phrase to guess
     * @param language The language of the secret word
     * @return Result indicating success or failure
     */
    suspend fun setSecretWord(roomId: String, secretWord: String, language: com.bramish.wheelofpasiva.domain.model.Language): Result<Unit>
}
