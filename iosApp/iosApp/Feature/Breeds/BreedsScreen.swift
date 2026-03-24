import SwiftUI
import NukeUI
import UIKit
import shared

private let swipeThreshold: CGFloat = 110
private let visibleCards = 3
private let backCardScaleStep: CGFloat = 0.04
private let backCardYOffsetStep: CGFloat = 18
private let topBarHeight: CGFloat = 64

struct BreedsScreen: View {
    @ObservedObject var viewModel: ViewModelWrapper
    let onNavigateToFavourites: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let state = viewModel.uiState

        ZStack(alignment: .top) {
            palette.background.ignoresSafeArea()

            BreedsContent(
                state: state,
                onSwipeLeft: viewModel.swipeLeft(breed:),
                onSwipeRight: viewModel.swipeRight(breed:)
            )

            BreedsTopBar(onNavigateToFavourites: onNavigateToFavourites)
                .padding(.top, 0)
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

private struct BreedsTopBar: View {
    let onNavigateToFavourites: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        HStack {
            Text("Posa")
                .font(.system(size: 24, weight: .black))
                .foregroundStyle(palette.onBackground)

            Spacer()

            Button(action: onNavigateToFavourites) {
                Image("favourite")
                    .renderingMode(.template)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 24, height: 24)
                    .foregroundStyle(palette.tertiary)
            }
            .buttonStyle(.plain)
            .padding(.trailing, 4)
        }
        .padding(.horizontal, 16)
        .frame(height: topBarHeight)
    }
}

private struct BreedsContent: View {
    let state: BreedsUiState
    let onSwipeLeft: (CatBreed) -> Void
    let onSwipeRight: (CatBreed) -> Void

    var body: some View {
        ZStack {
            if state.isLoading, state.deck.isEmpty {
                BreedsLoadingContent()
            } else if let message = state.error?.message, state.deck.isEmpty {
                BreedsErrorContent(message: message)
            } else if state.deck.isEmpty {
                BreedsDeckEmptyContent(reachedEnd: state.hasReachedEnd)
            } else {
                BreedsDeckContent(state: state, onSwipeLeft: onSwipeLeft, onSwipeRight: onSwipeRight)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct BreedsDeckContent: View {
    let state: BreedsUiState
    let onSwipeLeft: (CatBreed) -> Void
    let onSwipeRight: (CatBreed) -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        VStack(spacing: 0) {
            ZStack {
                let visibleBreeds = Array(state.deck.prefix(visibleCards))

                ForEach(Array(visibleBreeds.reversed().enumerated()), id: \.element.id) { reversedIdx, breed in
                    let depthIndex = visibleBreeds.count - 1 - reversedIdx

                    BreedCard(
                        breed: breed,
                        depthIndex: depthIndex,
                        onSwipeLeft: { onSwipeLeft(breed) },
                        onSwipeRight: { onSwipeRight(breed) }
                    )
                }
            }
            .animation(.interpolatingSpring(stiffness: 260, damping: 22), value: state.deck.first?.id ?? "")
            .padding(.top, topBarHeight)
            .padding(20)
            .frame(maxWidth: .infinity)
            .frame(maxHeight: .infinity)

            if state.isPrefetching {
                Text("Loading more…")
                    .font(.caption2)
                    .foregroundStyle(palette.surface.opacity(0.35))
                    .padding(.bottom, 4)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct BreedTopCard: View {
    let breed: CatBreed
    let onSwipeLeft: () -> Void
    let onSwipeRight: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    @State private var offset: CGSize = .zero
    @State private var hasPassedThreshold = false

    var body: some View {
        BreedCardContent(breed: breed, isInteractive: true)
            .overlay { SwipeLikeOverlay(offsetX: offset.width) }
            .overlay { SwipeNopeOverlay(offsetX: offset.width) }
            .offset(offset)
            .rotationEffect(.degrees(offset.width * 0.018))
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { value in
                        offset = value.translation

                        let crossed = abs(offset.width) > swipeThreshold
                        if crossed, !hasPassedThreshold {
                            let generator = UIImpactFeedbackGenerator(style: .heavy)
                            generator.impactOccurred()
                            hasPassedThreshold = true
                        } else if !crossed {
                            hasPassedThreshold = false
                        }
                    }
                    .onEnded { value in
                        let predictedX = value.predictedEndTranslation.width

                        if offset.width > swipeThreshold || predictedX > swipeThreshold * 2 {
                            withAnimation(.easeOut(duration: 0.36)) {
                                offset = CGSize(width: 2000, height: offset.height)
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
                                onSwipeRight()
                            }
                            return
                        }

                        if offset.width < -swipeThreshold || predictedX < -swipeThreshold * 2 {
                            withAnimation(.easeOut(duration: 0.36)) {
                                offset = CGSize(width: 2000, height: offset.height)
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
                                onSwipeLeft()
                            }
                            return
                        }

                        withAnimation(.interpolatingSpring(stiffness: 240, damping: 18)) {
                            offset = .zero
                        }
                        hasPassedThreshold = false
                    }
            )
            .onChange(of: breed.id) {
                offset = .zero
                hasPassedThreshold = false
            }
            .accessibilityElement(children: .combine)
            .accessibilityLabel(Text(breed.name))
            .accessibilityHint(Text("Swipe right to favourite, swipe left to dismiss"))
    }
}

private struct BreedCard: View {
    let breed: CatBreed
    let depthIndex: Int
    let onSwipeLeft: () -> Void
    let onSwipeRight: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    @State private var offset: CGSize = .zero
    @State private var hasPassedThreshold = false

    private var isTop: Bool { depthIndex == 0 }

    var body: some View {
        let scale = 1.0 - CGFloat(depthIndex) * backCardScaleStep
        let yOffset = CGFloat(depthIndex) * backCardYOffsetStep

        BreedCardContent(breed: breed, isInteractive: isTop)
            .overlay {
                if isTop {
                    SwipeLikeOverlay(offsetX: offset.width)
                }
            }
            .overlay {
                if isTop {
                    SwipeNopeOverlay(offsetX: offset.width)
                }
            }
            .scaleEffect(scale)
            .offset(x: isTop ? offset.width : 0, y: yOffset + (isTop ? offset.height : 0))
            .rotationEffect(.degrees(isTop ? (offset.width * 0.018) : 0))
            .zIndex(Double(100 - depthIndex))
            .animation(.interpolatingSpring(stiffness: 260, damping: 22), value: depthIndex)
            .allowsHitTesting(isTop)
            .gesture(dragGesture)
            .onChange(of: isTop) {
                if !isTop {
                    offset = .zero
                    hasPassedThreshold = false
                }
            }
            .onChange(of: breed.id) {
                offset = .zero
                hasPassedThreshold = false
            }
            .accessibilityElement(children: .combine)
            .accessibilityLabel(Text(breed.name))
            .accessibilityHint(Text("Swipe right to favourite, swipe left to dismiss"))
    }

    private var dragGesture: some Gesture {
        DragGesture(minimumDistance: 0)
            .onChanged { value in
                offset = value.translation

                let crossed = abs(offset.width) > swipeThreshold
                if crossed, !hasPassedThreshold {
                    let generator = UIImpactFeedbackGenerator(style: .heavy)
                    generator.impactOccurred()
                    hasPassedThreshold = true
                } else if !crossed {
                    hasPassedThreshold = false
                }
            }
            .onEnded { _ in
                if offset.width > swipeThreshold {
                    withAnimation(.interpolatingSpring(stiffness: 240, damping: 18)) {
                        offset = CGSize(width: 800, height: offset.height)
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
                        onSwipeRight()
                    }
                    return
                }

                if offset.width < -swipeThreshold {
                    triggerTrollSwipe()
                    return
                }

                withAnimation(.interpolatingSpring(stiffness: 240, damping: 18)) {
                    offset = .zero
                }
                hasPassedThreshold = false
            }
    }

    private func triggerTrollSwipe() {
        withAnimation(.interpolatingSpring(stiffness: 240, damping: 18)) {
            offset = CGSize(width: 800, height: offset.height)
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.36) {
            onSwipeLeft()
        }
    }
}

private struct BreedCardContent: View {
    let breed: CatBreed
    let isInteractive: Bool

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let overlay = palette.onSurface
        let corner = PosaRadii.medium

        ZStack(alignment: .bottom) {
            LazyImage(url: URL(string: breed.imageUrl), transaction: Transaction(animation: nil)) { state in
                if let image = state.image {
                    image
                        .centerCropped()
                } else {
                    Rectangle()
                        .fill(palette.surfaceVariant)
                }
            }
            .overlay {
                LinearGradient(
                    stops: [
                        .init(color: .clear, location: 0.0),
                        .init(color: overlay.opacity(0.7), location: 0.8),
                        .init(color: overlay.opacity(1.0), location: 1.0),
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            }

            VStack(alignment: .leading, spacing: 12) {
                BreedNameRow(breed: breed)
                TemperamentChips(temperaments: Array(breed.temperaments.prefix(3)))
                BreedTraitBars(traits: breed.traits)
                BreedBadgesRow(badges: breed.badges)
            }
            .padding(.horizontal, 18)
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))
        .shadow(color: Color.black.opacity(isInteractive ? 0.28 : 0.14), radius: isInteractive ? 20 : 6, x: 0, y: isInteractive ? 10 : 4)
    }
}

private struct BreedNameRow: View {
    let breed: CatBreed

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let secondary = palette.surface.opacity(0.75)

        HStack(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 2) {
                Text(breed.name)
                    .font(.system(size: 20, weight: .bold))
                    .foregroundStyle(palette.surface)

                if let alt = breed.altName, !alt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                    Text(alt)
                        .font(.footnote)
                        .foregroundStyle(secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            VStack(alignment: .trailing, spacing: 2) {
                Text(breed.origin)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(secondary)

                Text("🕑 \(breed.lifeSpan)")
                    .font(.caption2)
                    .foregroundStyle(secondary)
            }
        }
    }
}

private struct TemperamentChips: View {
    let temperaments: [String]

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        if temperaments.isEmpty {
            EmptyView()
        } else {
            let palette = PosaPalette(colorScheme: colorScheme)

            HStack(spacing: 6) {
                ForEach(temperaments, id: \.self) { label in
                    Text(label)
                        .font(.caption2)
                        .foregroundStyle(palette.onSecondaryContainer)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(palette.secondaryContainer)
                        .clipShape(RoundedRectangle(cornerRadius: PosaRadii.medium, style: .continuous))
                }
            }
        }
    }
}

private struct BreedTraitBars: View {
    let traits: CatTraits

    var body: some View {
        VStack(spacing: 5) {
            TraitRow(label: "Affection", level: traits.affectionLevel)
            TraitRow(label: "Energy", level: traits.energyLevel)
            TraitRow(label: "Grooming", level: traits.grooming)
        }
    }
}

private struct TraitRow: View {
    let label: String
    let level: Int32

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        HStack(spacing: 8) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(palette.surface)
                .frame(width: 60, alignment: .leading)

            PosaProgressBar(
                value: CGFloat(max(0, min(level, 5))) / 5.0,
                fill: palette.tertiaryContainer,
                track: palette.tertiary.opacity(0.25)
            )
            .frame(height: 5)
        }
    }
}

private struct PosaProgressBar: View {
    let value: CGFloat
    let fill: Color
    let track: Color

    var body: some View {
        GeometryReader { proxy in
            ZStack(alignment: .leading) {
                RoundedRectangle(cornerRadius: 999, style: .continuous)
                    .fill(track)

                RoundedRectangle(cornerRadius: 999, style: .continuous)
                    .fill(fill)
                    .frame(width: proxy.size.width * value)
            }
        }
    }
}

private struct BreedBadgesRow: View {
    let badges: CatBadges

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let active: [String] = {
            var result: [String] = []
            if badges.isIndoor { result.append("🏠 Indoor") }
            if badges.isHypoallergenic { result.append("🌿 Hypo") }
            if badges.isHairless { result.append("✨ Hairless") }
            if badges.hasShortLegs { result.append("🐾 Shorties") }
            if badges.isLap { result.append("❤️ Lap") }
            return result
        }()

        if active.isEmpty {
            EmptyView()
        } else {
            let palette = PosaPalette(colorScheme: colorScheme)

            HStack(spacing: 6) {
                ForEach(active, id: \.self) { badge in
                    Text(badge)
                        .font(.caption2)
                        .foregroundStyle(palette.onSecondaryContainer)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(palette.secondaryContainer)
                        .overlay {
                            RoundedRectangle(cornerRadius: PosaRadii.medium, style: .continuous)
                                .stroke(palette.onSecondaryContainer.opacity(0.25), lineWidth: 0.5)
                        }
                        .clipShape(RoundedRectangle(cornerRadius: PosaRadii.medium, style: .continuous))
                }
            }
        }
    }
}
