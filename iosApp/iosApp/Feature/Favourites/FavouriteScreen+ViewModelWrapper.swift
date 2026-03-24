import Combine
import KMPNativeCoroutinesCombine
import shared


extension FavouritesScreen {
    @MainActor
    final class ViewModelWrapper: ObservableObject {
        private let viewModel: FavouritesViewModel

        @Published private(set) var uiState: FavouritesUiState
        @Published private(set) var snackbarMessage: String?

        private var snackbarDismissTask: Task<Void, Never>?

        private var cancellables = Set<AnyCancellable>()

        init() {
            self.viewModel = IosViewModelProvider().favouritesViewModel
            self.uiState = FavouritesUiState(
                favourites: [],
                isLoading: true,
                error: nil,
                sortOrder: shared.SortOrder.desc,
            )
            self.snackbarMessage = nil

            createPublisher(for: viewModel.uiState)
                .receive(on: DispatchQueue.main)
                .sink(receiveCompletion: { _ in
                }, receiveValue: { [weak self] value in
                    self?.uiState = value
                })
                .store(in: &cancellables)

            createPublisher(for: viewModel.events)
                .receive(on: DispatchQueue.main)
                .sink(receiveCompletion: { _ in
                }, receiveValue: { [weak self] event in
                    guard let self else { return }

                    if event is FavouritesEventFavouriteRemoved {
                        self.showSnackbar(message: "🤨🤨🤨⁉️")
                    } else if let showError = event as? FavouritesEventShowError {
                        self.showSnackbar(message: showError.message)
                    }
                })
                .store(in: &cancellables)
        }

        private func showSnackbar(message: String) {
            snackbarDismissTask?.cancel()
            snackbarMessage = message
            snackbarDismissTask = Task { [weak self] in
                try? await Task.sleep(nanoseconds: 2_400_000_000)
                guard !Task.isCancelled else { return }
                await MainActor.run {
                    self?.snackbarMessage = nil
                }
            }
        }

        func onSortOrderChange(sortOrder: shared.SortOrder) {
            viewModel.onSortOrderChange(sortOrder: sortOrder)
        }

        func removeCard(favourite: FavouriteImage) {
            viewModel.remove(favourite: favourite)
        }

        deinit {
            snackbarDismissTask?.cancel()
            cancellables.forEach { $0.cancel() }
        }
    }
}
