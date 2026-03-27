//
//  SwipeLikeOverlay.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import Swift
import SwiftUI

struct SwipeStampView : View {
    let alignment: Alignment
    let label: String
    let color: Color
    let rotationDegrees: CGFloat
    let offsetX: CGFloat
    
    var body: some View {
        let alpha = (abs(offsetX) / 240.0).coerceIn(0, 1)
        
        GeometryReader { proxy in
            ZStack(alignment: alignment) {
                color.opacity(0.30)
                    .frame(height: proxy.size.height)
                
                ZStack(alignment: alignment) {
                    Text(label)
                        .textCase(.uppercase)
                        .font(.title.bold())
                        .tracking(2)
                        .foregroundStyle(color)
                }.padding(16)
                    .roundedBorder(color, width: 3, cornerRadius: 10)
                    .offset(x: 5, y: 30)
                    .rotationEffect(.degrees(rotationDegrees))
            }
            .opacity(alpha)
        }
    }
}

struct SwipeLikeOverlay : View {
    let offsetX: CGFloat
    
    var body: some View {
        SwipeStampView(
            alignment: .topLeading,
            label: "Yeyy",
            color: Color.green,
            rotationDegrees: -22,
            offsetX: offsetX
        )
    }
}

struct SwipeNopeOverlay : View {
    let offsetX: CGFloat
    
    var body: some View {
        SwipeStampView(
            alignment: .topTrailing,
            label: "Nayyy",
            color: Color.red,
            rotationDegrees: 22,
            offsetX: offsetX
        )
    }
}

private extension Double {
    func coerceIn(_ start: Double, _ end: Double) -> Double {
        return min(max(self, start), end)
    }
}

#Preview {
    HStack(spacing: 20) {
        SwipeLikeOverlay(offsetX: 200)
//        SwipeNopeOverlay(offsetX: 200)
    }
}
