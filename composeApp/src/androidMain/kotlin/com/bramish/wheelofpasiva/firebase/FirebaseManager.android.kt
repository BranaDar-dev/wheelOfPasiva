package com.bramish.wheelofpasiva.firebase

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.crashlytics

actual class FirebaseManager {
    
    companion object {
        @Volatile
        private var applicationContext: Context? = null
        
        fun setApplicationContext(context: Context) {
            applicationContext = context.applicationContext
        }
    }
    
    private val analytics: FirebaseAnalytics by lazy {
        val context = applicationContext 
            ?: throw IllegalStateException("Application context not set. Call FirebaseManager.setApplicationContext() in MainActivity.onCreate()")
        FirebaseAnalytics.getInstance(context)
    }
    
    actual fun initialize() {
        // Firebase is auto-initialized on Android via google-services.json
        // This method is kept for consistency with iOS, but Firebase is already initialized
        // when google-services.json is present and google-services plugin is applied
    }
    
    actual fun logEvent(eventName: String, parameters: Map<String, Any>?) {
        analytics.logEvent(eventName) {
            parameters?.forEach { (key, value) ->
                param(key, value.toString())
            }
        }
    }
    
    actual fun logException(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)
    }
    
    actual fun setCustomKey(key: String, value: String) {
        Firebase.crashlytics.setCustomKey(key, value)
    }
    
    actual fun setUserId(userId: String) {
        Firebase.crashlytics.setUserId(userId)
        analytics.setUserId(userId)
    }
}
