//
//  SnackbarManager.swift
//  Sumisetsu
//
//  Created by AL on 12/2/25.
//  Copyright © 2025 White Widget. All rights reserved.
//

import SwiftUI

final class SnackbarManager: ObservableObject {
    @Published var message: String = ""
    @Published var isShowing: Bool = false
    private var timer: Timer?

    func show(_ msg: String, duration: TimeInterval = 3, completion: @escaping () -> Void = {}) {
        withAnimation {
            timer?.invalidate()
            isShowing = false
        } completion: { [weak self] in
            self?.message = msg
            self?.isShowing = true
        }

        
        timer = Timer.scheduledTimer(withTimeInterval: duration, repeats: false) { timer in
            withAnimation {
                self.isShowing = false
            } completion: {
                completion()
            }
        }
    }
}
