package com.bramish.wheelofpasiva.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a game room.
 *
 * @property id 6-digit unique room identifier
 * @property createdAt Timestamp when the room was created
 * @property hostId ID of the player who created the room
 * @property players List of players currently in the room
 */
data class Room(
    val id: String,
    val createdAt: Instant,
    val hostId: String,
    val players: List<Player>
)
