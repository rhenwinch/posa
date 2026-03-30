//
//  EndOfListLabel.swift
//  iosApp
//
//  Created by Rhen on 3/26/26.
//

import SwiftUI
import shared

struct EndOfListLabelView : View {
    var body : some View {
        Text("— You've seen them all —")
            .font(.caption)
            .foregroundStyle(Color.appOnSurface.opacity(0.6))
            .padding(.vertical, 20)
            .frame(maxWidth: .infinity)
            .accessibilityIdentifier(UiIdentifiers.shared.FAVOURITES_END_OF_LIST)
    }
}

#Preview {
    EndOfListLabelView()
}
