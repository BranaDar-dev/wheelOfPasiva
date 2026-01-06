package com.bramish.wheelofpasiva.firebase

/**
 * Common interface for Firebase operations across platforms
 * This follows the expect/actual pattern for Kotlin Multiplatform
 */
expect class FirebaseManager {
    /**
     * Initialize Firebase (called automatically on Android, manually on iOS)
     */
    fun initialize()
    
    /**
     * Log an event to Firebase Analytics
     */
    fun logEvent(eventName: String, parameters: Map<String, Any>? = null)
    
    /**
     * Log a non-fatal exception to Crashlytics
     */
    fun logException(throwable: Throwable)
    
    /**
     * Set a custom key-value pair for Crashlytics
     */
    fun setCustomKey(key: String, value: String)
    
    /**
     * Set user ID for analytics and crashlytics
     */
    fun setUserId(userId: String)
}
