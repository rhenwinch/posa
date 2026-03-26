//
//  SnackbarView.swift
//  Sumisetsu
//
//  Created by AL on 12/2/25.
//  Copyright © 2025 White Widget. All rights reserved.
//

import SwiftUI

struct SnackbarView: View {
    @EnvironmentObject var snackbar: SnackbarManager

    var body: some View {
        if snackbar.isShowing && !snackbar.message.isEmpty {
            Text(snackbar.message)
                .font(.callout)
                .foregroundStyle(Color.appOnSurface)
                .multilineTextAlignment(.leading)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .frame(maxWidth: .infinity)
                .background(Color.appSurfaceContainerLow)
                .roundedBorder(Color.clear, cornerRadius: 5)
                .shadow(color: .appOnSurface.opacity(0.18), radius: 10, x: 0, y: 5)
                .padding(.horizontal, 16)
                .padding(.bottom, 10)
                .allowsHitTesting(false)
                .accessibilityElement(children: .combine)
                .accessibilityLabel(Text(snackbar.message))
                .transition(.move(edge: .bottom).combined(with: .opacity))
                .animation(.easeOut(duration: 0.3), value: snackbar.isShowing)
        }
    }
}
