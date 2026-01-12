package com.bramish.wheelofpasiva.data.datasource

import com.bramish.wheelofpasiva.data.model.PlayerDto
import com.bramish.wheelofpasiva.data.model.RoomDto
import com.bramish.wheelofpasiva.domain.model.RoomError
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Firestore data source using GitLive Firebase SDK.
 * Works on both Android and iOS without platform-specific implementations.
 */
class FirestoreDataSource {
    private val firestore = Firebase.firestore
    private val roomsCollection = firestore.collection("rooms")

    /**
     * Creates a new room in Firestore.
     */
    suspend fun createRoom(roomDto: RoomDto): Result<Unit> {
        return try {
            roomsCollection.document(roomDto.id).set(roomDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RoomError.NetworkError(e))
        }
    }

    /**
     * Checks if a room exists in Firestore.
     */
    suspend fun roomExists(roomId: String): Result<Boolean> {
        return try {
            val snapshot = roomsCollection.document(roomId).get()
            Result.success(snapshot.exists)
        } catch (e: Exception) {
            Result.failure(RoomError.NetworkError(e))
        }
    }

    /**
     * Adds a player to an existing room.
     */
    suspend fun addPlayer(roomId: String, playerDto: PlayerDto): Result<Unit> {
        return try {
            val documentRef = roomsCollection.document(roomId)
            val snapshot = documentRef.get()

            if (!snapshot.exists) {
                return Result.failure(RoomError.RoomNotFound(roomId))
            }

            val roomData = snapshot.data<RoomDto>()
            val updatedPlayers = roomData.players + playerDto
            val updatedRoom = roomData.copy(players = updatedPlayers)

            documentRef.set(updatedRoom)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(RoomError.NetworkError(e))
        }
    }

    /**
     * Observes real-time changes to a room.
     */
    fun observeRoom(roomId: String): Flow<Result<RoomDto>> {
        return roomsCollection.document(roomId).snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    Result.success(snapshot.data<RoomDto>())
                } else {
                    Result.failure(RoomError.RoomNotFound(roomId))
                }
            }
            .catch { e ->
                emit(Result.failure(RoomError.NetworkError(e)))
            }
    }

    /**
     * Gets a room by ID (one-time fetch).
     */
    suspend fun getRoom(roomId: String): Result<RoomDto> {
        return try {
            val snapshot = roomsCollection.document(roomId).get()

            if (!snapshot.exists) {
                return Result.failure(RoomError.RoomNotFound(roomId))
            }

            val roomDto = snapshot.data<RoomDto>()
            Result.success(roomDto)
        } catch (e: Exception) {
            Result.failure(RoomError.NetworkError(e))
        }
    }
}
