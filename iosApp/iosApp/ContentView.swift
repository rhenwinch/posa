import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack(alignment: .bottom) {
            BreedsScreen(onNavigateToFavouritesScreen: {})
        }
    }
}


#Preview {
    ContentView()
}
