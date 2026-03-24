import Combine
import KMPNativeCoroutinesCombine
import shared


extension BreedsScreen {
    @MainActor
    final class ViewModelWrapper: ObservableObject {
        private let viewModel: BreedsViewModel

        @Published private(set) var uiState: BreedsUiState
        @Published private(set) var snackbarMessage: String?

        private var snackbarDismissTask: Task<Void, Never>?

        private var cancellables = Set<AnyCancellable>()

        init() {
            self.viewModel = IosViewModelProvider().breedsViewModel
            self.uiState = BreedsUiState(
                deck: [],
                isLoading: true,
                isPrefetching: false,
                error: nil,
                hasReachedEnd: false
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

                    if event is BreedsEventFavouriteAdded {
                        self.showSnackbar(message: "Added to favourites ❤️")
                    } else if event is BreedsEventDismissedButHellNah {
                        self.showSnackbar(message: GetRandomMessageKt.getRandomNoSwipeMessage())
                    } else if let showError = event as? BreedsEventShowError {
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

        func swipeLeft(breed: CatBreed) {
            viewModel.swipeLeft(breed: breed)
        }

        func swipeRight(breed: CatBreed) {
            viewModel.swipeRight(breed: breed, troll: false)
        }

        deinit {
            snackbarDismissTask?.cancel()
            cancellables.forEach { $0.cancel() }
        }
    }
}
