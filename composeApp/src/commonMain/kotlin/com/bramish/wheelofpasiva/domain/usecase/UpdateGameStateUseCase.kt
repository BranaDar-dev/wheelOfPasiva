package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.repository.RoomRepository

/**
 * Use case for updating the game state in a room.
 * Updates all wheel game fields including scores, revealed letters, etc.
 */
class UpdateGameStateUseCase(
    private val roomRepository: RoomRepository
) {
    /**
     * Updates the game state in Firestore.
     *
     * @param roomId The room to update
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
    suspend operator fun invoke(
        roomId: String,
        isSpinning: Boolean? = null,
        nextTurnIndex: Int? = null,
        playerScores: Map<String, Int>? = null,
        revealedLetters: String? = null,
        lastSliceIndex: Int? = null,
        hasExtraTurn: Boolean? = null,
        isGameOver: Boolean? = null,
        winnerId: String? = null
    ): Result<Unit> {
        return roomRepository.updateGameState(
            roomId = roomId,
            isSpinning = isSpinning,
            nextTurnIndex = nextTurnIndex,
            playerScores = playerScores,
            revealedLetters = revealedLetters,
            lastSliceIndex = lastSliceIndex,
            hasExtraTurn = hasExtraTurn,
            isGameOver = isGameOver,
            winnerId = winnerId
        )
    }
}
