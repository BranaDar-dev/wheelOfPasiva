import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        // Initialize Firebase via Kotlin GitLive SDK
        FirebaseManager().initialize()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
