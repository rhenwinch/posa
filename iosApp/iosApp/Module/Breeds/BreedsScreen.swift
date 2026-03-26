//
//  BreedsScreen.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI
import shared
import NukeUI

private let topBarHeight = 64.0
private let visibleCards = 3

struct BreedsScreen : View {
    @StateObject private var viewModel = BreedsScreenViewModelWrapper()
    @StateObject private var snackbar = SnackbarManager()
    
    let onNavigateToFavouritesScreen: () -> Void
    
    var body: some View {
        ZStack(alignment: .top) {
            Color.appSurface.ignoresSafeArea()
            
            TopBar(onNavigateToFavouritesScreen: onNavigateToFavouritesScreen)
            
            if viewModel.isLoading && viewModel.deck.isEmpty {
                BreedsLoadingContent()
            } else if viewModel.error != nil && viewModel.deck.isEmpty {
                BreedsErrorContent(error: viewModel.error!)
            } else if viewModel.deck.isEmpty {
                BreedsDeckEmptyContent(reachedEnd: viewModel.hasReachedEnd)
            } else {
                BreedDeckContent(
                    isPrefetching: viewModel.isPrefetching,
                    deck: viewModel.deck,
                    onSwipeLeft: { viewModel.swipeLeft(breed: $0) },
                    onSwipeRight: { viewModel.swipeRight(breed: $0) },
                )
            }
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
    let onNavigateToFavouritesScreen: () -> Void
    
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [
                    .appOnSurface.opacity(0.3),
                    .appOnSurface.opacity(0)
                ],
                startPoint: .top,
                endPoint: .bottom
            ).ignoresSafeArea()
            
            HStack {
                Text("posa")
                    .font(.title)
                    .fontWeight(.black)
                    .foregroundStyle(Color.appOnSurface)
                
                Spacer()
                
                Button(action: onNavigateToFavouritesScreen) {
                    Image("favourite")
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(Color.appTertiary)
                }
            }
            .padding(.horizontal)
        }
        .frame(height: topBarHeight)
    }
}

private struct BreedDeckContent : View {
    let isPrefetching: Bool
    let deck: [CatBreed]
    
    let onSwipeLeft: (CatBreed) -> Void
    let onSwipeRight: (CatBreed) -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                let visibleBreeds = Array(deck.prefix(visibleCards))

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
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.top, topBarHeight)
            .padding(20)
            
            if isPrefetching {
                Text("Loading more…")
                    .font(.caption)
                    .foregroundStyle(Color.appOnSurface)
                    .opacity(0.35)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct BreedCard : View {
    let breed: CatBreed
    let depthIndex: Int
    
    let onSwipeLeft: () -> Void
    let onSwipeRight: () -> Void
    
    @State private var offset = CGSize.zero
    @State private var rotation = CGFloat.zero
    @State private var hasPassedThreshold = false
    
    var body: some View {
        GeometryReader { proxy in
            ZStack(alignment: .bottom) {
                LazyImage(url: URL(string: breed.imageUrl)) { state in
                    if let image = state.image {
                        image.centerCropped()
                    } else {
                        Rectangle()
                            .fill(Color.appSurfaceContainerHighest)
                    }
                }
                .overlay {
                    LinearGradient(
                        stops: [
                            .init(color: Color.clear, location: 0),
                            .init(color: Color.appOnSurface.opacity(0.8), location: 0.8),
                            .init(color: Color.appOnSurface.opacity(1.0), location: 1),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }
                
                VStack(alignment: .leading, spacing: 12) {
                    BreedNameRow(breed: breed)
                    BreedBadgesRow(badges: breed.badges)
                    TemperamentChips(temperaments: Array(breed.temperaments.prefix(3)))
                    BreedTraitBars(traits: breed.traits)
                }
                .padding(.horizontal, 18)
                .padding(.vertical, 14)
            }
            .overlay(alignment: .top) {
                if offset.width > 0 {
                    SwipeLikeOverlay(offsetX: offset.width)
                } else if offset.width < 0 {
                    SwipeNopeOverlay(offsetX: offset.width)
                }
            }
            .roundedBorder(Color.appSurface.opacity(0.6), width: 0.2, cornerRadius: 15,)
            .shadow(radius: CGFloat(depthIndex) * 20)
            .offset(offset)
            .rotationEffect(Angle(degrees: rotation))
            .offset(y: 30 * CGFloat(depthIndex))
            .scaleEffect(1.0 - (0.05 * CGFloat(depthIndex)))
            .animation(.easeOut, value: depthIndex == 0)
            .gesture(
                DragGesture()
                    .onChanged { gesture in
                        withAnimation(.bouncy) {
                            offset = gesture.translation
                            rotation = CGFloat(gesture.translation.width) * 0.018
                        }
                        
                        hasPassedThreshold = abs(gesture.translation.width) > (proxy.size.width / 2)
                    }
                    .onEnded { gesture in
                        if hasPassedThreshold {
                            let translation = gesture.translation
                            let isSwipingRight = translation.width > 0
                            
                            withAnimation(.easeOut(duration: 0.36)) {
                                rotation = CGFloat(abs(translation.width)) * 0.018
                                offset = CGSize(
                                    width: proxy.size.width * 2.0,
                                    height: translation.height,
                                )
                            } completion: {
                                if isSwipingRight {
                                    onSwipeRight()
                                } else {
                                    onSwipeLeft()
                                }
                            }
                            
                            return
                        }
                        
                        
                        withAnimation(.bouncy) {
                            offset = .zero
                            rotation = .zero
                        }
                    }
                ,
                isEnabled: depthIndex == 0
            )
            .sensoryFeedback(.impact, trigger: hasPassedThreshold) { oldValue, newValue in
                !oldValue && newValue
            }
        }
    }
}

private struct BreedNameRow : View {
    let breed: CatBreed
    
    var body: some View {
        HStack(alignment: .bottom) {
            VStack(alignment: .leading) {
                Text(breed.name)
                    .font(.title2.bold())
                
                if let altName = breed.altName {
                    Text(altName)
                        .font(.subheadline)
                        .opacity(0.75)
                }
            }
            .foregroundStyle(Color.appSurface)
            
            Spacer()
            
            VStack(alignment: .trailing) {
                Text(breed.origin)
                    .font(.caption.bold())
                
                Text("🕑 \(breed.lifeSpan) yrs")
                    .font(.caption2)
            }
            .foregroundStyle(Color.appSurface.opacity(0.75))
        }
    }
}

private struct TemperamentChips: View {
    let temperaments: [String]
    
    var body: some View {
        HStack(spacing: 6) {
            ForEach(temperaments, id: \.self) { temperament in
                Text(temperament)
                    .font(.caption.bold())
                    .padding(6)
                    .background(Color.appSecondaryContainer)
                    .foregroundStyle(Color.appOnSecondaryContainer)
                    .roundedBorder(Color.clear, cornerRadius: 5)
            }
        }
    }
}

private struct BreedTraitBars : View {
    let traits: CatTraits
    
    var body: some View {
        let displayTraits = [
            "Affection": traits.affectionLevel,
            "Energy": traits.energyLevel,
            "Intelligence": traits.intelligence,
            "Shedding": traits.sheddingLevel,
            "Health": traits.healthIssues,
        ]
        
        Grid(alignment: .leading, horizontalSpacing: 10, verticalSpacing: 6) {
            ForEach(displayTraits.sorted(by: >), id: \.key) { key, value in
                GridRow(alignment: .center) {
                    Text(key)
                        .font(.caption2)
                        .multilineTextAlignment(.leading)
                        .foregroundStyle(Color.appSurface)
                    
                    ZStack {
                        RoundedRectangle(cornerRadius: 15)
                            .fill(Color.appTertiary.opacity(0.6))
                        
                        GeometryReader { proxy in
                            RoundedRectangle(cornerRadius: 15)
                                .fill(Color.appTertiaryContainer)
                                .frame(width: CGFloat(value) / 5 * proxy.size.width)
                        }
                    }
                    .frame(height: 5)
                }
            }
        }
    }
}

private struct BreedBadgesRow : View {
    let badges: CatBadges
    
    var body: some View {
        let displayBadges = [
            badges.isIndoor ? "🏠 Indoor" : nil,
            badges.isHypoallergenic ? "🌿 Hypo" : nil,
            badges.isHairless ? "✨ Hairless" : nil,
            badges.hasShortLegs ? "🐾 Shorties" : nil,
            badges.isLap ? "❤️ Lap" : nil
        ].compactMap { $0 }
        
        HStack(spacing: 6) {
            ForEach(displayBadges, id: \.self) { badge in
                Text(badge)
                    .font(.caption.bold())
                    .padding(6)
                    .foregroundStyle(Color.appSecondaryContainer)
                    .roundedBorder(Color.appSecondaryContainer, cornerRadius: 5)
            }
        }
    }
}

#Preview {
    BreedsScreen(onNavigateToFavouritesScreen: {})
}
