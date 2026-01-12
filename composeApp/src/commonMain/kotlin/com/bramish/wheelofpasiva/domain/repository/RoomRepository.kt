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
}
