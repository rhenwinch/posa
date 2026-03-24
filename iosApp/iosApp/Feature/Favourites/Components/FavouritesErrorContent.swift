import SwiftUI

struct FavouritesErrorContent: View {
    let message: String

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        VStack(spacing: 0) {
            Text("😿")
                .font(.system(size: 48))

            Spacer().frame(height: 12)

            Text("Couldn't load favourites")
                .font(.headline)
                .fontWeight(.bold)

            Spacer().frame(height: 6)

            Text(message)
                .font(.footnote)
                .foregroundStyle(palette.onBackground.opacity(0.6))
        }
        .multilineTextAlignment(.center)
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
