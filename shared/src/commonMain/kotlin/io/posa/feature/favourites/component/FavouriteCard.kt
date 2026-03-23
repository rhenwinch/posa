package io.posa.feature.favourites.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import posa.shared.generated.resources.Res
import posa.shared.generated.resources.unfavourite


@Composable
internal fun FavouriteCard(
    favourite: FavouriteImage,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val overlay = MaterialTheme.colorScheme.onSurface

    var isBeingRemoved by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isBeingRemoved) 0.88f else 1f,
        animationSpec = tween(200),
        label = "FavCardScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isBeingRemoved) 0f else 1f,
        animationSpec = tween(220),
        label = "FavCardAlpha",
    )

    LaunchedEffect(isBeingRemoved) {
        if (isBeingRemoved) {
            delay(180)
            onRemove()
        }
    }

    Card(
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            hoveredElevation = 6.dp
        ),
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
    ) {
        Box {
            AsyncImage(
                model = favourite.imageUrl,
                contentDescription = favourite.breed.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                0.5f to Color.Transparent,
                                1.00f to overlay,
                            )
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f), CircleShape),
            ) {
                IconButton(
                    onClick = { isBeingRemoved = true },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.unfavourite),
                        contentDescription = "Remove from favourites",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = favourite.breed.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.surface,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = favourite.breed.origin,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    maxLines = 1,
                )
            }
        }
    }
}