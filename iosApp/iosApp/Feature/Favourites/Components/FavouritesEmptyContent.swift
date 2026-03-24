import SwiftUI

struct FavouritesEmptyContent: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        VStack(spacing: 0) {
            Text("🐾")
                .font(.system(size: 48))

            Spacer().frame(height: 12)

            Text("No favourites yet")
                .font(.headline)
                .fontWeight(.bold)

            Spacer().frame(height: 6)

            Text("Swipe right on a breed to save it here.")
                .font(.footnote)
                .foregroundStyle(palette.onBackground.opacity(0.6))
        }
        .multilineTextAlignment(.center)
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
