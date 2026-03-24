import SwiftUI
import shared

extension Color {
    init(posaARGB32 argb: UInt32) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8) & 0xFF) / 255.0
        let b = Double(argb & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }

    init(posaARGB argb: Int64) {
        self.init(posaARGB32: UInt32(truncatingIfNeeded: argb))
    }
}

struct PosaPalette: Sendable {
    let colorScheme: ColorScheme

    var primary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.PrimaryDark) : .init(posaARGB: Colors.shared.PrimaryLight) }
    var onPrimary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnPrimaryDark) : .init(posaARGB: Colors.shared.OnPrimaryLight) }

    var primaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.PrimaryContainerDark) : .init(posaARGB: Colors.shared.PrimaryContainerLight) }
    var onPrimaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnPrimaryContainerDark) : .init(posaARGB: Colors.shared.OnPrimaryContainerLight) }

    var secondary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.SecondaryDark) : .init(posaARGB: Colors.shared.SecondaryLight) }
    var onSecondary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnSecondaryDark) : .init(posaARGB: Colors.shared.OnSecondaryLight) }

    var secondaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.SecondaryContainerDark) : .init(posaARGB: Colors.shared.SecondaryContainerLight) }
    var onSecondaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnSecondaryContainerDark) : .init(posaARGB: Colors.shared.OnSecondaryContainerLight) }

    var tertiary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.TertiaryDark) : .init(posaARGB: Colors.shared.TertiaryLight) }
    var onTertiary: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnTertiaryDark) : .init(posaARGB: Colors.shared.OnTertiaryLight) }

    var tertiaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.TertiaryContainerDark) : .init(posaARGB: Colors.shared.TertiaryContainerLight) }
    var onTertiaryContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnTertiaryContainerDark) : .init(posaARGB: Colors.shared.OnTertiaryContainerLight) }

    var background: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.BackgroundDark) : .init(posaARGB: Colors.shared.BackgroundLight) }
    var onBackground: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnBackgroundDark) : .init(posaARGB: Colors.shared.OnBackgroundLight) }

    var surface: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.SurfaceDark) : .init(posaARGB: Colors.shared.SurfaceLight) }
    var onSurface: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnSurfaceDark) : .init(posaARGB: Colors.shared.OnSurfaceLight) }

    var surfaceVariant: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.SurfaceVariantDark) : .init(posaARGB: Colors.shared.SurfaceVariantLight) }
    var onSurfaceVariant: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnSurfaceVariantDark) : .init(posaARGB: Colors.shared.OnSurfaceVariantLight) }

    var surfaceContainerLow: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.SurfaceContainerLowDark) : .init(posaARGB: Colors.shared.SurfaceContainerLowLight) }

    var error: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.ErrorDark) : .init(posaARGB: Colors.shared.ErrorLight) }
    var onError: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnErrorDark) : .init(posaARGB: Colors.shared.OnErrorLight) }

    var errorContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.ErrorContainerDark) : .init(posaARGB: Colors.shared.ErrorContainerLight) }
    var onErrorContainer: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OnErrorContainerDark) : .init(posaARGB: Colors.shared.OnErrorContainerLight) }

    var outline: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OutlineDark) : .init(posaARGB: Colors.shared.OutlineLight) }
    var outlineVariant: Color { colorScheme == .dark ? .init(posaARGB: Colors.shared.OutlineVariantDark) : .init(posaARGB: Colors.shared.OutlineVariantLight) }
}

enum PosaRadii {
    static let small: CGFloat = 8
    static let medium: CGFloat = 12
}

struct PosaSnackbar: View {
    let message: String

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        Text(message)
            .font(.callout.weight(.semibold))
            .foregroundStyle(palette.onSurface)
            .multilineTextAlignment(.center)
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .frame(maxWidth: .infinity)
            .background(palette.surfaceContainerLow)
            .overlay {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(palette.outlineVariant.opacity(0.6), lineWidth: 1)
            }
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .shadow(color: palette.onSurface.opacity(0.18), radius: 10, x: 0, y: 5)
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
            .allowsHitTesting(false)
            .accessibilityElement(children: .combine)
            .accessibilityLabel(Text(message))
    }
}
