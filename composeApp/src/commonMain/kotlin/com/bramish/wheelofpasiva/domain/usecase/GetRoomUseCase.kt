package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.repository.RoomRepository

/**
 * Use case for retrieving a room by ID (one-time fetch).
 *
 * @property repository The room repository for data operations
 */
class GetRoomUseCase(
    private val repository: RoomRepository
) {
    /**
     * Retrieves a room by its ID.
     *
     * @param roomId The ID of the room to retrieve
     * @return Result containing the room or an error
     */
    suspend operator fun invoke(roomId: String): Result<Room> {
        return repository.getRoom(roomId)
    }
}
