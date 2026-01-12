package com.bramish.wheelofpasiva.domain.util

import kotlinx.datetime.Instant

/**
 * Platform-agnostic time provider.
 * Workaround for Clock.System not being available on all platforms in kotlinx-datetime 0.6.0.
 */
expect object TimeProvider {
    fun now(): Instant
}
