package io.posa.feature.breeds.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.posa.core.common.UiIdentifiers

private val LikeGreen = Color(0xFF44C766)

@Composable
internal fun SwipeLikeOverlay(offsetX: Animatable<Float, AnimationVector1D>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .graphicsLayer { alpha = (offsetX.value / 240f).coerceIn(0f, 1f) }
            .background(LikeGreen.copy(alpha = 0.30f)),
        contentAlignment = Alignment.TopStart,
    ) {
        SwipeStamp(
            text = "LIKE",
            color = LikeGreen,
            rotation = -22f,
            modifier = Modifier
                .padding(22.dp)
                .testTag(UiIdentifiers.BREEDS_OVERLAY_LIKE)
                .semantics { contentDescription = "Like" },
        )
    }
}