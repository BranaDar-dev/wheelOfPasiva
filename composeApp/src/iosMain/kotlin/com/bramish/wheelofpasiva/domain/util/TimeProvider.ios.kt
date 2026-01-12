package com.bramish.wheelofpasiva.domain.util

import kotlinx.datetime.Instant
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object TimeProvider {
    actual fun now(): Instant {
        val timeInterval = NSDate().timeIntervalSince1970
        return Instant.fromEpochSeconds(timeInterval.toLong(), (timeInterval % 1 * 1_000_000_000).toInt())
    }
}
