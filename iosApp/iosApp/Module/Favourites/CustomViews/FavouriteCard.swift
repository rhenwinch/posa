//
//  FavouriteCard.swift
//  iosApp
//
//  Created by Rhen on 3/26/26.
//

import SwiftUI
import NukeUI
import shared

struct FavouriteCard : View {
    let favourite: FavouriteImage
    let onRemove: () -> Void
    let onClick: () -> Void
    
    @State private var scale: CGFloat = 1
    @State private var opacity: CGFloat = 1
    
    var body: some View {
        ZStack(alignment: .topTrailing) {
            ZStack(alignment: .bottomLeading) {
                LazyImage(url: URL(string: favourite.imageUrl)) { state in
                    if let image = state.image {
                        image
                            .centerCropped()
                    } else {
                        Rectangle()
                            .fill(Color.appSurfaceContainerHighest)
                    }
                }
                .overlay {
                    LinearGradient(
                        stops: [
                            .init(color: .clear, location: 0.4),
                            .init(color: Color.appOnSurface, location: 1.0),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(favourite.breed.name)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(Color.appSurface)
                        .lineLimit(1)

                    Text(favourite.breed.origin)
                        .font(.caption2)
                        .foregroundStyle(Color.appSurface.opacity(0.6))
                        .lineLimit(1)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .onTapGesture { onClick() }
            
            Button(
                action: onRemove,
                label: {
                    ZStack {
                        Circle()
                            .fill(Color.appTertiaryContainer.opacity(0.8))

                        Image("unfavourite")
                            .renderingMode(.template)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 14, height: 14)
                            .foregroundStyle(Color.appOnTertiaryContainer)
                    }
                    .frame(width: 28, height: 28)
                }
            )
            .accessibilityIdentifier(UiIdentifiers.shared.favouritesItemRemove(imageId: favourite.imageId))
            .buttonStyle(.plain)
            .padding(8)
        }
        .aspectRatio(1, contentMode: .fit)
        .contentShape(Rectangle())
        .roundedBorder(Color.appSurface.opacity(0.6), width: 0.4, cornerRadius: 5)
        .scaleEffect(scale)
        .opacity(opacity)
        .shadow(radius: 4)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(UiIdentifiers.shared.favouritesItem(imageId: favourite.imageId))
    }
}

#Preview {
    let columns = [GridItem(.adaptive(minimum: 200)), GridItem(.adaptive(minimum: 200))]
    
    ScrollView {
        LazyVGrid(columns: columns) {
            var images = Array(mockFavouriteImages)
            ForEach(Array(images.enumerated()), id: \.offset) { i, value in
                FavouriteCard(
                    favourite: value,
                    onRemove: { images.remove(at: i) },
                    onClick: {}
                )
            }
        }
        .padding(10)
    }
}
