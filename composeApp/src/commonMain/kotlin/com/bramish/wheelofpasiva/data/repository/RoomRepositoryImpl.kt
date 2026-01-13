package com.bramish.wheelofpasiva.data.repository

import com.bramish.wheelofpasiva.data.datasource.FirestoreDataSource
import com.bramish.wheelofpasiva.data.model.PlayerDto
import com.bramish.wheelofpasiva.data.model.RoomDto
import com.bramish.wheelofpasiva.domain.model.Player
import com.bramish.wheelofpasiva.domain.model.Room
import com.bramish.wheelofpasiva.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of RoomRepository that uses FirestoreDataSource.
 * Transforms between domain models and data DTOs.
 *
 * @property dataSource The Firestore data source for database operations
 */
class RoomRepositoryImpl(
    private val dataSource: FirestoreDataSource
) : RoomRepository {

    override suspend fun createRoom(room: Room): Result<Unit> {
        val roomDto = RoomDto.fromDomain(room)
        return dataSource.createRoom(roomDto)
    }

    override suspend fun roomExists(roomId: String): Result<Boolean> {
        return dataSource.roomExists(roomId)
    }

    override suspend fun addPlayer(roomId: String, player: Player): Result<Unit> {
        val playerDto = PlayerDto.fromDomain(player)
        return dataSource.addPlayer(roomId, playerDto)
    }

    override fun observeRoom(roomId: String): Flow<Result<Room>> {
        return dataSource.observeRoom(roomId).map { result ->
            result.map { roomDto -> roomDto.toDomain() }
        }
    }

    override suspend fun getRoom(roomId: String): Result<Room> {
        return dataSource.getRoom(roomId).map { roomDto ->
            roomDto.toDomain()
        }
    }

    override suspend fun updateGameState(
        roomId: String,
        isSpinning: Boolean?,
        nextTurnIndex: Int?,
        playerScores: Map<String, Int>?,
        revealedLetters: String?,
        lastSliceIndex: Int?,
        hasExtraTurn: Boolean?,
        isGameOver: Boolean?,
        winnerId: String?
    ): Result<Unit> {
        return dataSource.updateGameState(
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

    override suspend fun startGame(roomId: String): Result<Unit> {
        return dataSource.startGame(roomId)
    }

    override suspend fun setSecretWord(roomId: String, secretWord: String, language: com.bramish.wheelofpasiva.domain.model.Language): Result<Unit> {
        return dataSource.setSecretWord(roomId, secretWord, language.name)
    }
}
