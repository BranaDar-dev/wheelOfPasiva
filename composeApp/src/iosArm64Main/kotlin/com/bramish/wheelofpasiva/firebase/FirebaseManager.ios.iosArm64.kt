package com.bramish.wheelofpasiva.firebase

actual class FirebaseWrapper {
    actual fun initialize() {
    }

    actual fun logEventWithEventName(
        eventName: String,
        parameters: platform.Foundation.NSMutableDictionary?
    ) {
    }

    actual fun logExceptionWithMessage(message: String, stackTrace: String?) {
    }

    actual fun setCustomKeyWithKey(key: String, value: String) {
    }

    actual fun setUserIdWithUserId(userId: String) {
    }

    actual companion object {
        actual fun shared(): FirebaseWrapper {
            TODO("Not yet implemented")
        }
    }
}