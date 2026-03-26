//
//  BreedsScreenViewModelWrapper.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCombine

extension BreedsScreen {
    
    @MainActor
    class BreedsScreenViewModelWrapper : ObservableObject {
        private let viewModel: BreedsViewModel
        private var cancellables =  Set<AnyCancellable>()
        
        @Published var hasReachedEnd: Bool = false
        @Published var isPrefetching: Bool = false
        @Published var isLoading: Bool = false
        @Published var error: String?
        @Published var message: String?
        @Published var deck: [CatBreed] = []
        
        
        init() {
            viewModel = IosViewModelProvider().breedsViewModel
            
            createPublisher(for: viewModel.uiState)
                .receive(on: DispatchQueue.main)
                .sink(
                    receiveCompletion: { _ in },
                    receiveValue: { uiState in
                        self.hasReachedEnd = uiState.hasReachedEnd
                        self.isPrefetching = uiState.isPrefetching
                        self.isLoading = uiState.isLoading
                        self.error = uiState.error?.message
                        self.deck = uiState.deck
                    }
                )
                .store(in: &cancellables)
            
            createPublisher(for: viewModel.events)
                .receive(on: DispatchQueue.main)
                .sink(
                    receiveCompletion: { _ in },
                    receiveValue: { event in
                        if event is BreedsEventFavouriteAdded {
                            self.message = "Added to favourites ❤️"
                        } else if event is BreedsEventDismissedButHellNah {
                            self.message = GetRandomMessageKt.getRandomNoSwipeMessage()
                        } else if event is BreedsEventShowError {
                            self.message = (event as? BreedsEventShowError)?.message
                        }
                    }
                )
                .store(in: &cancellables)
        }

        func swipeRight(breed: CatBreed, troll: Bool = false) {
            viewModel.swipeRight(breed: breed, troll: troll)
        }
        
        func swipeLeft(breed: CatBreed) {
            viewModel.swipeLeft(breed: breed)
        }
        
        func consumeMessage() {
            self.message = nil
        }
    }
}
