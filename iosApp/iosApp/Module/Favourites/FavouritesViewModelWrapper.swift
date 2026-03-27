//
//  FavouritesViewModelWrapper.swift
//  iosApp
//
//  Created by Rhen on 3/26/26.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

extension FavouritesScreen {
    @MainActor
    class FavouritesViewModelWrapper : ObservableObject {
        private let viewModel: FavouritesViewModel
        private var cancellables =  Set<AnyCancellable>()
        
        @Published var favourties: [FavouriteImage] = []
        @Published var isLoading: Bool = false
        @Published var error: String?
        @Published var message: String?
        @Published var sortOrder: shared.SortOrder = SortOrder.desc
        
        
        init() {
            viewModel = IosViewModelProvider().favouritesViewModel
            
            createPublisher(for: viewModel.uiState)
                .receive(on: DispatchQueue.main)
                .sink(
                    receiveCompletion: { _ in },
                    receiveValue: { uiState in
                        self.favourties = uiState.favourites
                        self.isLoading = uiState.isLoading
                        self.error = uiState.error?.message
                        self.sortOrder = uiState.sortOrder
                    }
                )
                .store(in: &cancellables)
            
            createPublisher(for: viewModel.events)
                .receive(on: DispatchQueue.main)
                .sink(
                    receiveCompletion: { _ in },
                    receiveValue: { event in
                        if event is FavouritesEventFavouriteRemoved {
                            self.message = "🤨🤨🤨⁉️"
                        } else if event is FavouritesEventShowError {
                            self.message = (event as? FavouritesEventShowError)?.message
                        }
                    }
                )
                .store(in: &cancellables)
        }
        
        func consumeMessage() {
            self.message = nil
        }
        
        func onSortOrderChange(sortOrder: shared.SortOrder) {
            viewModel.onSortOrderChange(sortOrder: sortOrder)
        }
        
        func remove(favourite: FavouriteImage) {
            viewModel.remove(favourite: favourite)
        }
    }
}
