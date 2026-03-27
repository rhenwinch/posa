//
//  BreedsLoadingContent.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI

struct LoadingView : View {
    let message: String
    
    var body: some View {
        VStack(alignment: .center, spacing: 20) {
            CircularProgressView(size: 35)
            
            Text(message)
                .font(.body)
                .fontWeight(.medium)
                .foregroundStyle(Color.appOnSurface)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct CircularProgressView: View {
    var color: Color = .appPrimary
    var size: CGFloat = 48
    var strokeWidth: CGFloat = 4

    @State private var isAnimating = false

    var body: some View {
        Circle()
            .trim(from: 0.0, to: 0.75)
            .stroke(
                color,
                style: StrokeStyle(
                    lineWidth: strokeWidth,
                    lineCap: .round
                )
            )
            .frame(width: size, height: size)
            .rotationEffect(.degrees(isAnimating ? 360 : 0))
            .animation(
                .linear(duration: 1.0)
                .repeatForever(autoreverses: false),
                value: isAnimating
            )
            .onAppear { isAnimating = true }
            .onDisappear { isAnimating = false }
    }
}

#Preview {
    LoadingView(message: "Finding cats…")
}
