package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.repository.RoomRepository
import com.bramish.wheelofpasiva.domain.util.TimeProvider
import kotlin.random.Random

/**
 * Use case for joining an existing room.
 * Validates the room ID format, checks if the room exists, and adds the player.
 *
 * @property repository The room repository for data operations
 */
class JoinRoomUseCase(
    private val repository: RoomRepository
) {
    /**
     * Joins a room with the given room ID and player nickname.
     *
     * @param roomId The 6-digit room ID to join
     * @param nickname The player's nickname
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(roomId: String, nickname: String): Result<Unit> {
        // Validate room ID format (must be 6 digits)
        if (!roomId.matches(Regex("^\\d{6}$"))) {
            return Result.failure(RoomError.InvalidRoomId(roomId))
        }

        if (nickname.isBlank()) {
            return Result.failure(IllegalArgumentException("Nickname cannot be empty"))
        }

        // Check if room exists
        val roomExists = repository.roomExists(roomId).getOrElse {
            return Result.failure(it)
        }

        if (!roomExists) {
            return Result.failure(RoomError.RoomNotFound(roomId))
        }

        // Create player and add to room
        val playerId = generatePlayerId()
        val player = Player(
            id = playerId,
            nickname = nickname,
            joinedAt = TimeProvider.now()
        )

        return repository.addPlayer(roomId, player)
    }

    /**
     * Generates a unique player ID using a simple timestamp-based approach.
     */
    private fun generatePlayerId(): String {
        return "${TimeProvider.now().toEpochMilliseconds()}_${Random.nextInt(1000, 10000)}"
    }
}
