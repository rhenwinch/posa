import SwiftUI

enum Screen: Hashable {
    case favourites
    case breeds
}

struct ContentView: View {
    @State private var path = NavigationPath()
    
    var body: some View {
        NavigationStack(path: $path) {
            BreedsScreen(
                onNavigateToFavouritesScreen: {
                    path.append(Screen.breeds)
                }
            )
            .navigationDestination(for: Screen.self) { _ in
                FavouritesScreen(
                    onBack: {
                        path.removeLast()
                    }
                )
                .navigationBarBackButtonHidden()
            }
        }
    }
}


#Preview {
    ContentView()
}
