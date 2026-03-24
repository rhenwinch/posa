import SwiftUI

struct EndOfListLabel: View {
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        Text("— You've seen them all —")
            .font(.caption2)
            .foregroundStyle(palette.onBackground.opacity(0.6))
            .tracking(0.5)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 20)
    }
}
