import SwiftUI
import NukeUI
import shared

struct FavouriteCard: View {
    let favourite: FavouriteImage
    let onRemove: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    @State private var isBeingRemoved = false

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let overlay = palette.onSurface

        let scale: CGFloat = isBeingRemoved ? 0.88 : 1.0
        let alpha: CGFloat = isBeingRemoved ? 0.0 : 1.0

        ZStack(alignment: .topTrailing) {
            ZStack(alignment: .bottomLeading) {
                LazyImage(url: URL(string: favourite.imageUrl)) { state in
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
                            .init(color: .clear, location: 0.5),
                            .init(color: overlay, location: 1.0),
                        ],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(favourite.breed.name)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(palette.surface)
                        .lineLimit(1)

                    Text(favourite.breed.origin)
                        .font(.caption2)
                        .foregroundStyle(palette.surface.opacity(0.6))
                        .lineLimit(1)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity, alignment: .leading)
            }

            Button {
                withAnimation(.easeOut(duration: 0.2)) {
                    isBeingRemoved = true
                }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.18) {
                    onRemove()
                }
            } label: {
                ZStack {
                    Circle()
                        .fill(palette.tertiaryContainer.opacity(0.8))

                    Image("unfavourite")
                        .renderingMode(.template)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 14, height: 14)
                        .foregroundStyle(palette.onTertiaryContainer)
                }
                .frame(width: 28, height: 28)
            }
            .buttonStyle(.plain)
            .padding(8)
        }
        .clipShape(RoundedRectangle(cornerRadius: PosaRadii.small, style: .continuous))
        .shadow(color: Color.black.opacity(0.16), radius: 3, x: 0, y: 2)
        .aspectRatio(1, contentMode: .fit)
        .scaleEffect(scale)
        .opacity(alpha)
        .animation(.easeOut(duration: 0.2), value: isBeingRemoved)
    }
}
