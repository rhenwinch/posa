//
//  Styling.swift
//  iosApp
//
//  Created by Rhen on 3/25/26.
//

import SwiftUI
import shared

extension Color {
    private static func dynamicColor(light: Int64, dark: Int64) -> Color {
        Color(uiColor: UIColor(dynamicProvider: { traitCollection in
            traitCollection.userInterfaceStyle == .dark
                ? UIColor(argb: dark)
                : UIColor(argb: light)
        }))
    }

    // MARK: - Primary

    static var appPrimary: Color {
        dynamicColor(light: Colors.shared.PrimaryLight, dark: Colors.shared.PrimaryDark)
    }
    static var appPrimaryContainer: Color {
        dynamicColor(light: Colors.shared.PrimaryContainerLight, dark: Colors.shared.PrimaryContainerDark)
    }
    static var appInversePrimary: Color {
        dynamicColor(light: Colors.shared.InversePrimaryLight, dark: Colors.shared.InversePrimaryDark)
    }

    // MARK: - On Primary

    static var appOnPrimary: Color {
        dynamicColor(light: Colors.shared.OnPrimaryLight, dark: Colors.shared.OnPrimaryDark)
    }
    static var appOnPrimaryContainer: Color {
        dynamicColor(light: Colors.shared.OnPrimaryContainerLight, dark: Colors.shared.OnPrimaryContainerDark)
    }

    // MARK: - Secondary

    static var appSecondary: Color {
        dynamicColor(light: Colors.shared.SecondaryLight, dark: Colors.shared.SecondaryDark)
    }
    static var appSecondaryContainer: Color {
        dynamicColor(light: Colors.shared.SecondaryContainerLight, dark: Colors.shared.SecondaryContainerDark)
    }

    // MARK: - On Secondary

    static var appOnSecondary: Color {
        dynamicColor(light: Colors.shared.OnSecondaryLight, dark: Colors.shared.OnSecondaryDark)
    }
    static var appOnSecondaryContainer: Color {
        dynamicColor(light: Colors.shared.OnSecondaryContainerLight, dark: Colors.shared.OnSecondaryContainerDark)
    }

    // MARK: - Tertiary

    static var appTertiary: Color {
        dynamicColor(light: Colors.shared.TertiaryLight, dark: Colors.shared.TertiaryDark)
    }
    static var appTertiaryContainer: Color {
        dynamicColor(light: Colors.shared.TertiaryContainerLight, dark: Colors.shared.TertiaryContainerDark)
    }

    // MARK: - On Tertiary

    static var appOnTertiary: Color {
        dynamicColor(light: Colors.shared.OnTertiaryLight, dark: Colors.shared.OnTertiaryDark)
    }
    static var appOnTertiaryContainer: Color {
        dynamicColor(light: Colors.shared.OnTertiaryContainerLight, dark: Colors.shared.OnTertiaryContainerDark)
    }

    // MARK: - Surface

    static var appSurface: Color {
        dynamicColor(light: Colors.shared.SurfaceLight, dark: Colors.shared.SurfaceDark)
    }
    static var appSurfaceVariant: Color {
        dynamicColor(light: Colors.shared.SurfaceVariantLight, dark: Colors.shared.SurfaceVariantDark)
    }
    static var appSurfaceBright: Color {
        dynamicColor(light: Colors.shared.SurfaceBrightLight, dark: Colors.shared.SurfaceBrightDark)
    }
    static var appSurfaceDim: Color {
        dynamicColor(light: Colors.shared.SurfaceDimLight, dark: Colors.shared.SurfaceDimDark)
    }
    static var appSurfaceTint: Color {
        dynamicColor(light: Colors.shared.SurfaceTintLight, dark: Colors.shared.SurfaceTintDark)
    }
    static var appInverseSurface: Color {
        dynamicColor(light: Colors.shared.InverseSurfaceLight, dark: Colors.shared.InverseSurfaceDark)
    }

    // MARK: - Surface Container

    static var appSurfaceContainer: Color {
        dynamicColor(light: Colors.shared.SurfaceContainerLight, dark: Colors.shared.SurfaceContainerDark)
    }
    static var appSurfaceContainerHigh: Color {
        dynamicColor(light: Colors.shared.SurfaceContainerHighLight, dark: Colors.shared.SurfaceContainerHighDark)
    }
    static var appSurfaceContainerHighest: Color {
        dynamicColor(light: Colors.shared.SurfaceContainerHighestLight, dark: Colors.shared.SurfaceContainerHighestDark)
    }
    static var appSurfaceContainerLow: Color {
        dynamicColor(light: Colors.shared.SurfaceContainerLowLight, dark: Colors.shared.SurfaceContainerLowDark)
    }
    static var appSurfaceContainerLowest: Color {
        dynamicColor(light: Colors.shared.SurfaceContainerLowestLight, dark: Colors.shared.SurfaceContainerLowestDark)
    }

    // MARK: - On Surface

    static var appOnSurface: Color {
        dynamicColor(light: Colors.shared.OnSurfaceLight, dark: Colors.shared.OnSurfaceDark)
    }
    static var appOnSurfaceVariant: Color {
        dynamicColor(light: Colors.shared.OnSurfaceVariantLight, dark: Colors.shared.OnSurfaceVariantDark)
    }
    static var appInverseOnSurface: Color {
        dynamicColor(light: Colors.shared.InverseOnSurfaceLight, dark: Colors.shared.InverseOnSurfaceDark)
    }
}

// MARK: - UIColor ARGB bridge

private extension UIColor {
    convenience init(argb: Int64) {
        self.init(
            red:   CGFloat((argb >> 16) & 0xFF) / 255.0,
            green: CGFloat((argb >> 8)  & 0xFF) / 255.0,
            blue:  CGFloat( argb        & 0xFF) / 255.0,
            alpha: CGFloat((argb >> 24) & 0xFF) / 255.0
        )
    }
}

