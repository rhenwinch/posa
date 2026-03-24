import SwiftUI

private enum Route: Hashable {
    case favourites
}

struct ContentView: View {
    @State private var path: [Route] = []

    @StateObject private var breedsViewModel = BreedsScreen.ViewModelWrapper()
    @StateObject private var favouritesViewModel = FavouritesScreen.ViewModelWrapper()

    var body: some View {
        NavigationStack(path: $path) {
            BreedsScreen(
                viewModel: breedsViewModel,
                onNavigateToFavourites: { path.append(.favourites) }
            )
            .toolbarVisibility(.hidden, for: .navigationBar)
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .favourites:
                    FavouritesScreen(
                        viewModel: favouritesViewModel
                    )
                    .toolbarVisibility(.hidden, for: .navigationBar)
                }
            }
        }
    }
}



