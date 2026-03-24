import SwiftUI

struct SwipeStamp: View {
    let text: String
    let color: Color
    let rotationDegrees: Double

    var body: some View {
        Text(text)
            .font(.system(size: 22, weight: .heavy))
            .tracking(2)
            .foregroundStyle(color)
            .padding(.horizontal, 12)
            .padding(.vertical, 4)
            .background(Color.clear)
            .overlay {
                RoundedRectangle(cornerRadius: PosaRadii.small, style: .continuous)
                    .stroke(color, lineWidth: 3)
            }
            .rotationEffect(.degrees(rotationDegrees))
    }
}
