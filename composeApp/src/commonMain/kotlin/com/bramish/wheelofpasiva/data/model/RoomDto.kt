package com.bramish.wheelofpasiva.data.model

import com.bramish.wheelofpasiva.domain.model.Room
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Room entity in Firestore.
 * Used for serialization/deserialization with Firestore.
 */
@Serializable
data class RoomDto(
    val id: String = "",
    val createdAt: Long = 0L, // Timestamp in milliseconds
    val hostId: String = "",
    val players: List<PlayerDto> = emptyList(),
    val isGameStarted: Boolean = false,
    val currentTurnIndex: Int = 0,
    val isSpinning: Boolean = false,
    val secretWord: String? = null,
    val language: String = "ENGLISH", // Language of the secret word
    val playerScores: Map<String, Int> = emptyMap(),
    val revealedLetters: String = "",
    val lastSliceIndex: Int? = null,
    val hasExtraTurn: Boolean = false,
    val isGameOver: Boolean = false,
    val winnerId: String? = null
) {
    /**
     * Converts DTO to domain model.
     */
    fun toDomain(): Room {
        return Room(
            id = id,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            hostId = hostId,
            players = players.map { it.toDomain() },
            isGameStarted = isGameStarted,
            currentTurnIndex = currentTurnIndex,
            isSpinning = isSpinning,
            secretWord = secretWord,
            language = com.bramish.wheelofpasiva.domain.model.Language.fromString(language),
            playerScores = playerScores,
            revealedLetters = revealedLetters,
            lastSliceIndex = lastSliceIndex,
            hasExtraTurn = hasExtraTurn,
            isGameOver = isGameOver,
            winnerId = winnerId
        )
    }

    /**
     * Converts to Map for Firestore.
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "createdAt" to createdAt,
            "hostId" to hostId,
            "players" to players.map { player ->
                mapOf(
                    "id" to player.id,
                    "nickname" to player.nickname,
                    "joinedAt" to player.joinedAt
                )
            },
            "isGameStarted" to isGameStarted,
            "currentTurnIndex" to currentTurnIndex,
            "isSpinning" to isSpinning,
            "secretWord" to secretWord,
            "language" to language,
            "playerScores" to playerScores,
            "revealedLetters" to revealedLetters,
            "lastSliceIndex" to lastSliceIndex,
            "hasExtraTurn" to hasExtraTurn,
            "isGameOver" to isGameOver,
            "winnerId" to winnerId
        )
    }

    companion object {
        /**
         * Creates DTO from domain model.
         */
        fun fromDomain(room: Room): RoomDto {
            return RoomDto(
                id = room.id,
                createdAt = room.createdAt.toEpochMilliseconds(),
                hostId = room.hostId,
                players = room.players.map { PlayerDto.fromDomain(it) },
                isGameStarted = room.isGameStarted,
                currentTurnIndex = room.currentTurnIndex,
                isSpinning = room.isSpinning,
                secretWord = room.secretWord,
                language = room.language.name,
                playerScores = room.playerScores,
                revealedLetters = room.revealedLetters,
                lastSliceIndex = room.lastSliceIndex,
                hasExtraTurn = room.hasExtraTurn,
                isGameOver = room.isGameOver,
                winnerId = room.winnerId
            )
        }

        /**
         * Creates DTO from Firestore Map.
         */
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): RoomDto {
            val playersData = map["players"] as? List<Map<String, Any>> ?: emptyList()
            val players = playersData.map { playerMap ->
                PlayerDto(
                    id = playerMap["id"] as? String ?: "",
                    nickname = playerMap["nickname"] as? String ?: "",
                    joinedAt = (playerMap["joinedAt"] as? Number)?.toLong() ?: 0L
                )
            }

            // Parse playerScores map
            val scoresData = map["playerScores"] as? Map<String, Any> ?: emptyMap()
            val playerScores = scoresData.mapValues { (_, value) ->
                (value as? Number)?.toInt() ?: 0
            }

            return RoomDto(
                id = map["id"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                hostId = map["hostId"] as? String ?: "",
                players = players,
                isGameStarted = map["isGameStarted"] as? Boolean ?: false,
                currentTurnIndex = (map["currentTurnIndex"] as? Number)?.toInt() ?: 0,
                isSpinning = map["isSpinning"] as? Boolean ?: false,
                secretWord = map["secretWord"] as? String,
                language = map["language"] as? String ?: "ENGLISH",
                playerScores = playerScores,
                revealedLetters = map["revealedLetters"] as? String ?: "",
                lastSliceIndex = (map["lastSliceIndex"] as? Number)?.toInt(),
                hasExtraTurn = map["hasExtraTurn"] as? Boolean ?: false,
                isGameOver = map["isGameOver"] as? Boolean ?: false,
                winnerId = map["winnerId"] as? String
            )
        }
    }
}
