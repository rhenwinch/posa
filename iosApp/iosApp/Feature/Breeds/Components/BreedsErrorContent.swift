import SwiftUI

struct BreedsErrorContent: View {
    let message: String

    var body: some View {
        VStack(spacing: 0) {
            Text("😿")
                .font(.system(size: 48))

            Spacer().frame(height: 12)

            Text("Something went wrong")
                .font(.headline)
                .fontWeight(.bold)

            Spacer().frame(height: 6)

            Text(message)
                .font(.footnote)
        }
        .multilineTextAlignment(.center)
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
