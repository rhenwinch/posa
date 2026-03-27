package io.posa.feature.breeds.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp


@Composable
internal fun BreedsDeckEmptyContent(reachedEnd: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(if (reachedEnd) "breeds:empty:end" else "breeds:empty:loadingMore"),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (reachedEnd) "🐱" else "⏳",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (reachedEnd) "You've seen all breeds!" else "Loading more cats…",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}