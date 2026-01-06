import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    
    init() {
        // Initialize Firebase
        FirebaseWrapper.shared.initialize()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}