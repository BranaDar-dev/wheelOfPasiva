package com.bramish.wheelofpasiva.domain.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

actual object TimeProvider {
    actual fun now(): Instant = Clock.System.now()
}
