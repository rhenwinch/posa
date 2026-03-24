import SwiftUI

struct BreedsLoadingContent: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        VStack(spacing: 0) {
            ProgressView()
                .tint(palette.tertiary)
                .scaleEffect(1.1)

            Spacer().frame(height: 16)

            Text("Finding cats…")
                .font(.body)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
