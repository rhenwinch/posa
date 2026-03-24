import SwiftUI

struct SwipeLikeOverlay: View {
    let offsetX: CGFloat

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)
        let color = palette.tertiaryContainer
        let alpha = min(max(offsetX / 240.0, 0.0), 1.0)

        ZStack(alignment: .topLeading) {
            RoundedRectangle(cornerRadius: PosaRadii.medium, style: .continuous)
                .fill(color.opacity(0.30))

            SwipeStamp(text: "LIKE", color: color, rotationDegrees: -22)
                .padding(22)
        }
        .opacity(alpha)
    }
}
