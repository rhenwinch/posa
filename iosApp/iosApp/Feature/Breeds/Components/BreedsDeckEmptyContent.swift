import SwiftUI

struct BreedsDeckEmptyContent: View {
    let reachedEnd: Bool

    var body: some View {
        VStack(spacing: 12) {
            Text(reachedEnd ? "🐱" : "⏳")
                .font(.system(size: 48))

            Text(reachedEnd ? "You've seen all breeds!" : "Loading more cats…")
                .font(.body)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
