//
//  BreedsErrorContent..swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI

struct BreedsErrorContent : View {
    let error: String
    
    var body: some View {
        VStack(alignment: .center, spacing: 6) {
            Text("😿")
                .font(.largeTitle)
            
            Text("Something went wrong")
                .font(.title2)
                .fontWeight(.bold)
                .padding(.bottom, 6)
            
            Text(error)
                .font(.body)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .padding()
        .multilineTextAlignment(.center)
        .foregroundStyle(Color.appOnSurface)
    }
}

#Preview {
    BreedsErrorContent(error: "A connection error occured, check your internet and try again")
}
