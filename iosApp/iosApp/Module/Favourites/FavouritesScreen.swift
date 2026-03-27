//
//  FavouritesScreen.swift
//  iosApp
//
//  Created by Rhen on 3/26/26.
//

import SwiftUI
import shared


private let topBarHeight = 64.0

struct SheetData: Identifiable {
    let id: String
    let breed: CatBreed
}

struct FavouritesScreen : View {
    let onBack: () -> Void
    
    @StateObject private var viewModel = FavouritesViewModelWrapper()
    @StateObject private var snackbar = SnackbarManager()
    
    @State private var clickedBreed: SheetData?
    
    
    private let columns = [
        GridItem(.adaptive(minimum: 200)),
        GridItem(.adaptive(minimum: 200))
    ]
    
    var body: some View {
        ZStack(alignment: .top) {
            Color.appSurface.ignoresSafeArea()
            
            ScrollView {
                LazyVGrid(columns: columns, alignment: .leading) {
                    Section {
                        
                    } header: {
                        SortOrderToggle(
                            sortOrder: viewModel.sortOrder,
                            onChange: {
                                viewModel.onSortOrderChange(sortOrder: $0)
                            }
                        )
                    }
                    
                    ForEach(Array(viewModel.favourties.enumerated()), id: \.offset) { i, value in
                        FavouriteCard(
                            favourite: value,
                            onRemove: { viewModel.remove(favourite: value) },
                            onClick: {
                                clickedBreed = SheetData(
                                    id: value.imageId,
                                    breed: value.breed
                                )
                            },
                        )
                    }
                }
                .padding(.top, topBarHeight)
                .padding(10)
            }
            
            TopBar(
                itemCount: viewModel.favourties.count,
                onBack: onBack
            )
        }
        .sheet(
            item: $clickedBreed,
            onDismiss: {
                clickedBreed = nil
            }
        ) { value in
            BreedDetailSheet(breed: value.breed)
        }
        .overlay(alignment: .bottom) {
            SnackbarView()
                .environmentObject(snackbar)
        }
        .onChange(of: viewModel.message) {
            if let message = viewModel.message {
                snackbar.show(
                    message,
                    completion: { viewModel.consumeMessage() }
                )
            }
        }
        .animation(.easeOut(duration: 0.22), value: viewModel.message)
    }
}

private struct TopBar : View {
    let itemCount: Int
    let onBack: () -> Void
    
    var body: some View {
        ZStack {
            Color.appSurface.ignoresSafeArea()
            
            LinearGradient(
                colors: [
                    .appOnSurface.opacity(0.3),
                    .appOnSurface.opacity(0)
                ],
                startPoint: .top,
                endPoint: .bottom
            ).ignoresSafeArea()
            
            HStack {
                Button(action: onBack) {
                    Image("back")
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                }
                
                Text("Favourites")
                    .font(.title2.bold())
                    .padding(.trailing, 8)
                
                if itemCount > 0 {
                    Text(String(itemCount))
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(Color.appSurfaceContainer)
                        .roundedBorder(Color.clear, cornerRadius: 10)
                }
                
                Spacer()
            }
            .foregroundStyle(Color.appOnSurface)
            .padding(.horizontal)
            .shadow(radius: 30)
        }
        .frame(height: topBarHeight)
    }
}

private struct SortOrderToggle : View {
    let sortOrder: shared.SortOrder
    let onChange: (shared.SortOrder) -> Void
    
    private let sortOrders = [SortOrder.asc, SortOrder.desc]
    
    var body: some View {
        HStack {
            ForEach(sortOrders, id: \.self.name) { value in
                Button {
                    onChange(value)
                } label: {
                    Text(value.name)
                        .font(.caption)
                        .fontWeight(sortOrder == value ? .bold : .regular)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7)
                        .background(sortOrder == value ? Color.appPrimaryContainer : .clear)
                        .foregroundStyle(sortOrder == value ? Color.appOnPrimaryContainer : Color.appOnSurface.opacity(0.6))
                        .roundedBorder(.clear, cornerRadius: 20)
                        .animation(.easeOut(duration: 0.2), value: sortOrder)
                }
            }
        }
    }
}


#Preview {
    FavouritesScreen(onBack: {})
}
