package io.posa.feature.favourites.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.posa.core.common.UiIdentifiers


@Composable
internal fun FavouritesLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiIdentifiers.FAVOURITES_LOADING),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading favourites…",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalTextStyle.current.color.copy(alpha = 0.6f),
            )
        }
    }
}