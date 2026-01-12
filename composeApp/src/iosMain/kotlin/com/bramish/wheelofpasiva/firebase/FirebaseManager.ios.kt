package com.bramish.wheelofpasiva.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

/**
 * iOS implementation of FirebaseManager using GitLive SDK.
 */
actual class FirebaseManager {

    actual fun initialize() {
        // Initialize Firebase using GitLive SDK
        Firebase.initialize()
    }

    actual fun logEvent(eventName: String, parameters: Map<String, Any>?) {
        // Analytics can be added later with dev.gitlive:firebase-analytics
        println("FirebaseManager.logEvent: $eventName")
    }

    actual fun logException(throwable: Throwable) {
        // Crashlytics can be added later with dev.gitlive:firebase-crashlytics
        println("FirebaseManager.logException: ${throwable.message}")
    }

    actual fun setCustomKey(key: String, value: String) {
        println("FirebaseManager.setCustomKey: $key = $value")
    }

    actual fun setUserId(userId: String) {
        println("FirebaseManager.setUserId: $userId")
    }
}
