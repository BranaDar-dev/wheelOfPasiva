package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.repository.RoomRepository

/**
 * Use case for starting the game in a room.
 * Updates the isGameStarted flag in Firestore so all players navigate to the game screen.
 */
class StartGameUseCase(
    private val roomRepository: RoomRepository
) {
    /**
     * Starts the game in the specified room.
     *
     * @param roomId The room to start the game in
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(roomId: String): Result<Unit> {
        return roomRepository.startGame(roomId)
    }
}
