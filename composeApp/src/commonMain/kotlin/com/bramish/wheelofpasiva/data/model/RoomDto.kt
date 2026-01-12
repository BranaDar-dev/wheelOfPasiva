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
    val players: List<PlayerDto> = emptyList()
) {
    /**
     * Converts DTO to domain model.
     */
    fun toDomain(): Room {
        return Room(
            id = id,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            hostId = hostId,
            players = players.map { it.toDomain() }
        )
    }

    /**
     * Converts to Map for Firestore.
     */
    fun toMap(): Map<String, Any> {
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
            }
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
                players = room.players.map { PlayerDto.fromDomain(it) }
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

            return RoomDto(
                id = map["id"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                hostId = map["hostId"] as? String ?: "",
                players = players
            )
        }
    }
}
