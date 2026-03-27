//
//  BreedsDeckEmptyContent.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI

struct FavouritesEmptyView : View {
    var body: some View {
        VStack(alignment: .center, spacing: 6) {
            Text("🐾")
                .font(.largeTitle)
                .padding(.bottom, 6)
            
            Text("No favourites yet")
                .font(.title2.bold())
            
            Text("Start swiping to save it here!")
                .font(.body)
                .opacity(0.6)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .multilineTextAlignment(.center)
        .foregroundStyle(Color.appOnSurface)
    }
}

#Preview {
    VStack(spacing: 20) {
        FavouritesEmptyView()
    }
}
