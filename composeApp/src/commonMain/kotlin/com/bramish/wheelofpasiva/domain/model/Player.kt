package com.bramish.wheelofpasiva.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a player in a room.
 *
 * @property id Unique identifier for the player
 * @property nickname Display name chosen by the player
 * @property joinedAt Timestamp when the player joined the room
 */
data class Player(
    val id: String,
    val nickname: String,
    val joinedAt: Instant
)
