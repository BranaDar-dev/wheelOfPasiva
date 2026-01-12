package com.bramish.wheelofpasiva.domain.usecase

import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing real-time changes to a room and its players.
 *
 * @property repository The room repository for data operations
 */
class ObserveRoomUseCase(
    private val repository: RoomRepository
) {
    /**
     * Observes real-time updates to a room.
     *
     * @param roomId The ID of the room to observe
     * @return Flow emitting room updates or errors
     */
    operator fun invoke(roomId: String): Flow<Result<Room>> {
        return repository.observeRoom(roomId)
    }
}
