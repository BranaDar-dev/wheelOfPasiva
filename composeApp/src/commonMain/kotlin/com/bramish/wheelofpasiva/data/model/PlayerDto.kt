package com.bramish.wheelofpasiva.data.model

import com.bramish.wheelofpasiva.domain.model.Player
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Player entity in Firestore.
 * Used for serialization/deserialization with Firestore.
 */
@Serializable
data class PlayerDto(
    val id: String = "",
    val nickname: String = "",
    val joinedAt: Long = 0L // Timestamp in milliseconds
) {
    /**
     * Converts DTO to domain model.
     */
    fun toDomain(): Player {
        return Player(
            id = id,
            nickname = nickname,
            joinedAt = Instant.fromEpochMilliseconds(joinedAt)
        )
    }

    /**
     * Converts to Map for Firestore.
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nickname" to nickname,
            "joinedAt" to joinedAt
        )
    }

    companion object {
        /**
         * Creates DTO from domain model.
         */
        fun fromDomain(player: Player): PlayerDto {
            return PlayerDto(
                id = player.id,
                nickname = player.nickname,
                joinedAt = player.joinedAt.toEpochMilliseconds()
            )
        }
    }
}
