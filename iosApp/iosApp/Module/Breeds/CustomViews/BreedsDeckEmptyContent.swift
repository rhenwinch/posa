//
//  BreedsDeckEmptyContent.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI

struct BreedsDeckEmptyContent : View {
    let reachedEnd: Bool
    
    var body: some View {
        VStack(alignment: .center, spacing: 12) {
            Text(getHeaderEmoji())
                .font(.largeTitle)
            
            Text(getBodyText())
                .font(.title2)
                .fontWeight(.bold)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .multilineTextAlignment(.center)
        .foregroundStyle(Color.appOnSurface)
    }
    
    private func getHeaderEmoji() -> String {
        return reachedEnd ? "🐱" : "⏳"
    }
    
    private func getBodyText() -> String {
        return reachedEnd ? "You've seen all breeds!" : "Loading more cats…"
    }
}

#Preview {
    VStack(spacing: 20) {
        BreedsDeckEmptyContent(reachedEnd: true)
        
        BreedsDeckEmptyContent(reachedEnd: false)
    }
}
