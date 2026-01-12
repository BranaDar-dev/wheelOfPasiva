package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.model.RoomError
import com.bramish.wheelofpasiva.domain.repository.RoomRepository
import com.bramish.wheelofpasiva.domain.util.TimeProvider
import kotlin.random.Random

/**
 * Result of creating a room, containing both room and player IDs.
 */
data class CreateRoomResult(
    val roomId: String,
    val playerId: String
)

/**
 * Use case for creating a new room.
 * Generates a unique 6-digit room ID and creates the room in the database.
 *
 * @property repository The room repository for data operations
 */
class CreateRoomUseCase(
    private val repository: RoomRepository
) {
    /**
     * Creates a new room with the given host nickname.
     *
     * @param hostNickname The nickname of the player creating the room
     * @return Result containing the room ID and player ID, or an error
     */
    suspend operator fun invoke(hostNickname: String): Result<CreateRoomResult> {
        if (hostNickname.isBlank()) {
            return Result.failure(IllegalArgumentException("Nickname cannot be empty"))
        }

        // Try to generate a unique room ID (retry up to 5 times)
        repeat(5) {
            val roomId = generateRoomId()

            // Check if room ID already exists
            val exists = repository.roomExists(roomId).getOrElse {
                return Result.failure(it)
            }

            if (!exists) {
                // Generate player ID and create room
                val playerId = generatePlayerId()
                val now = TimeProvider.now()

                val player = Player(
                    id = playerId,
                    nickname = hostNickname,
                    joinedAt = now
                )

                val room = Room(
                    id = roomId,
                    createdAt = now,
                    hostId = playerId,
                    players = listOf(player)
                )

                return repository.createRoom(room).map { CreateRoomResult(roomId, playerId) }
            }
        }

        return Result.failure(RoomError.RoomIdGenerationFailed(null))
    }

    /**
     * Generates a random 6-digit room ID.
     */
    private fun generateRoomId(): String {
        return Random.nextInt(100000, 1000000).toString()
    }

    /**
     * Generates a unique player ID using a simple timestamp-based approach.
     * In production, consider using UUID or a more robust ID generation strategy.
     */
    private fun generatePlayerId(): String {
        return "${TimeProvider.now().toEpochMilliseconds()}_${Random.nextInt(1000, 10000)}"
    }
}
