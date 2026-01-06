import Foundation
import FirebaseCore
import FirebaseAnalytics
import FirebaseCrashlytics
import FirebaseFirestore

@objc public class FirebaseWrapper: NSObject {
    
    @objc public static let shared = FirebaseWrapper()
    
    private override init() {
        super.init()
    }
    
    @objc public func initialize() {
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
    }
    
    @objc public func logEvent(eventName: String, parameters: [String: Any]?) {
        Analytics.logEvent(eventName, parameters: parameters)
    }
    
    @objc public func logException(message: String, stackTrace: String?) {
        let error = NSError(
            domain: "com.bramish.wheelofpasiva",
            code: -1,
            userInfo: [
                NSLocalizedDescriptionKey: message,
                "stackTrace": stackTrace ?? "No stack trace"
            ]
        )
        Crashlytics.crashlytics().record(error: error)
    }
    
    @objc public func setCustomKey(key: String, value: String) {
        Crashlytics.crashlytics().setCustomValue(value, forKey: key)
    }
    
    @objc public func setUserId(userId: String) {
        Crashlytics.crashlytics().setUserID(userId)
        Analytics.setUserID(userId)
    }
}
