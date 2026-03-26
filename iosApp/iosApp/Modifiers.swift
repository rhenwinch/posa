//
//  Modifiers.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI

// Source - https://stackoverflow.com/a/57756280
// Posted by user7014451, modified by community. See post 'Timeline' for change history
// Retrieved 2026-03-25, License - CC BY-SA 4.0

extension View {
    public func roundedBorder<S>(_ content: S, width: CGFloat = 1, cornerRadius: CGFloat) -> some View where S : ShapeStyle {
         let roundedRect = RoundedRectangle(cornerRadius: cornerRadius)
         return clipShape(roundedRect)
              .overlay(roundedRect.strokeBorder(content, lineWidth: width))
    }
}

extension Image {
    func centerCropped() -> some View {
        GeometryReader { geo in
            self
                .resizable()
                .scaledToFill()
                .frame(width: geo.size.width, height: geo.size.height)
                .clipped()
        }
    }
}
