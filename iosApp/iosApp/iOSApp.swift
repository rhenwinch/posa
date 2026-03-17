import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        KoinInitializer().initKoinByPlatform()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}