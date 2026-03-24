import SwiftUI
import shared

struct SortOrderToggle: View {
    let sortOrder: shared.SortOrder
    let onSortOrderChange: (shared.SortOrder) -> Void

    var body: some View {
        HStack(spacing: 0) {
            SortChip(
                label: "Asc",
                selected: sortOrder.isAscending,
                onClick: { onSortOrderChange(.asc) }
            )

            SortChip(
                label: "Desc",
                selected: sortOrder.isDescending,
                onClick: { onSortOrderChange(.desc) }
            )
        }
    }
}

private struct SortChip: View {
    let label: String
    let selected: Bool
    let onClick: () -> Void

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let palette = PosaPalette(colorScheme: colorScheme)

        let selectedColor = palette.primaryContainer
        let selectedText = palette.onPrimaryContainer
        let unselectedText = palette.onBackground.opacity(0.6)

        Button(action: onClick) {
            Text(label)
                .font(.system(size: 12, weight: selected ? .bold : .regular))
                .foregroundStyle(selected ? selectedText : unselectedText)
                .padding(.horizontal, 14)
                .padding(.vertical, 7)
        }
        .buttonStyle(.plain)
        .background(selected ? selectedColor : Color.clear)
        .clipShape(Capsule())
        .animation(.easeOut(duration: 0.2), value: selected)
    }
}
