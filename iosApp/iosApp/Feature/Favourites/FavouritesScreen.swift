import SwiftUI
import shared

private let paginationPrefetchDistance = 4

struct FavouritesScreen: View {
    @ObservedObject var viewModel: ViewModelWrapper

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let state = viewModel.uiState

        ZStack(alignment: .top) {
            palette.background.ignoresSafeArea()

            FavouritesContent(
                state: state,
                onRemoveCard: viewModel.removeCard(favourite:),
                onSortOrderChange: viewModel.onSortOrderChange(sortOrder:)
            )
            .padding(.top, 64)

            FavouritesTopBar(count: state.favourites.count)
        }
        .overlay(alignment: .bottom) {
            if let message = viewModel.snackbarMessage {
                PosaSnackbar(message: message)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.easeOut(duration: 0.22), value: viewModel.snackbarMessage)
    }
}

private struct FavouritesTopBar: View {
    let count: Int

    @Environment(\.dismiss) private var dismiss

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        HStack(spacing: 12) {
            Button(action: { dismiss() }) {
                Image("back")
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
                    .foregroundStyle(palette.onBackground)
            }
            .buttonStyle(.plain)

            HStack(spacing: 8) {
                Text("Favourites")
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(palette.onBackground)

                if count > 0 {
                    Text("\(count)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundStyle(palette.onBackground.opacity(0.6))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(palette.surfaceContainerLow)
                        .clipShape(Capsule())
                }
            }

            Spacer()
        }
        .padding(.horizontal, 16)
        .frame(height: 64)
    }
}

private struct FavouritesContent: View {
    let state: FavouritesUiState
    let onRemoveCard: (FavouriteImage) -> Void
    let onSortOrderChange: (shared.SortOrder) -> Void

    var body: some View {
        ZStack {
            if state.isLoading, state.favourites.isEmpty {
                FavouritesLoadingContent()
            } else if let message = state.error?.message, state.favourites.isEmpty {
                FavouritesErrorContent(message: message)
            } else if state.favourites.isEmpty {
                FavouritesEmptyContent()
            } else {
                FavouritesGridContent(
                    state: state,
                    onRemoveCard: onRemoveCard,
                    onSortOrderChange: onSortOrderChange
                )
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct FavouritesGridContent: View {
    let state: FavouritesUiState
    let onRemoveCard: (FavouriteImage) -> Void
    let onSortOrderChange: (shared.SortOrder) -> Void

    var body: some View {
        let columns: [GridItem] = [
            GridItem(.adaptive(minimum: 160), spacing: 12),
            GridItem(.adaptive(minimum: 160), spacing: 12)
        ]

        ScrollView {
            LazyVGrid(columns: columns, alignment: .leading, spacing: 12) {
                Section {} header: {
                    SortOrderToggle(sortOrder: state.sortOrder, onSortOrderChange: onSortOrderChange)
                }
                
                ForEach(state.favourites, id: \.id) { favourite in
                    FavouriteCard(favourite: favourite) {
                        onRemoveCard(favourite)
                    }
                }

                if !state.favourites.isEmpty {
                    Section {} footer: {
                        EndOfListLabel()
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 24)
        }
    }
}
