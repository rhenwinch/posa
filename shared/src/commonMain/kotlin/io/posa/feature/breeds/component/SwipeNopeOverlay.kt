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
import androidx.compose.ui.unit.dp

private val NopeRed = Color(0xFFFF4D4D)

@Composable
internal fun SwipeNopeOverlay(offsetX: Animatable<Float, AnimationVector1D>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.medium)
            .graphicsLayer { alpha = (-offsetX.value / 240f).coerceIn(0f, 1f) }
            .background(NopeRed.copy(alpha = 0.30f)),
        contentAlignment = Alignment.TopEnd,
    ) {
        SwipeStamp(
            text = "NOPE",
            color = NopeRed,
            rotation = 22f,
            modifier = Modifier.padding(22.dp),
        )
    }
}