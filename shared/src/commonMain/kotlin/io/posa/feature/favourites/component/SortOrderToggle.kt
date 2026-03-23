package io.posa.feature.favourites.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.posa.core.common.enum.SortOrder

@Composable
internal fun SortOrderToggle(
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        SortChip(
            label = "Name",
            selected = sortOrder.isAscending,
            onClick = { onSortOrderChange(SortOrder.ASC) },
        )

        SortChip(
            label = "Added on",
            selected = sortOrder.isDescending,
            onClick = { onSortOrderChange(SortOrder.DESC) },
        )
    }
}


@Composable
private fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer

    val unselectedTextColor = LocalContentColor.current.copy(alpha = 0.6f)

    val bgColor by animateColorAsState(
        targetValue = if (selected) selectedColor else Color.Transparent,
        animationSpec = tween(200),
        label = "SortChipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) selectedTextColor else unselectedTextColor,
        animationSpec = tween(200),
        label = "SortChipText",
    )
    Surface(
        shape = CircleShape,
        color = bgColor,
        contentColor = contentColorFor(bgColor),
        onClick = onClick,
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}