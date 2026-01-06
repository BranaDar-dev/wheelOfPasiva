package com.bramish.wheelofpasiva.firebase

import platform.Foundation.NSMutableDictionary

actual class FirebaseManager {
    
    private val wrapper = FirebaseWrapper.shared()
    
    actual fun initialize() {
        wrapper.initialize()
    }
    
    actual fun logEvent(eventName: String, parameters: Map<String, Any>?) {
        val nsDict = parameters?.let { map ->
            val dict = NSMutableDictionary()
            map.forEach { (key, value) ->
//                dict.setValue(value, forKey = key)
            }
            dict
        }
        wrapper.logEventWithEventName(eventName, parameters = nsDict)
    }
    
    actual fun logException(throwable: Throwable) {
        val message = throwable.message ?: "Unknown error"
        val stackTrace = throwable.stackTraceToString()
        wrapper.logExceptionWithMessage(message, stackTrace = stackTrace)
    }
    
    actual fun setCustomKey(key: String, value: String) {
        wrapper.setCustomKeyWithKey(key, value = value)
    }
    
    actual fun setUserId(userId: String) {
        wrapper.setUserIdWithUserId(userId)
    }
}

// External declaration for the Swift wrapper

expect class FirebaseWrapper {
    companion object {
        fun shared(): FirebaseWrapper
    }
    fun initialize()
    fun logEventWithEventName(eventName: String, parameters: NSMutableDictionary?)
    fun logExceptionWithMessage(message: String, stackTrace: String?)
    fun setCustomKeyWithKey(key: String, value: String)
    fun setUserIdWithUserId(userId: String)
}
