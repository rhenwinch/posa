package io.posa.feature.breeds

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.posa.R
import io.posa.core.common.UiIdentifiers
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.feature.breeds.component.BreedsDeckEmptyContent
import io.posa.feature.breeds.component.BreedsErrorContent
import io.posa.feature.breeds.component.BreedsLoadingContent
import io.posa.feature.breeds.component.SwipeLikeOverlay
import io.posa.feature.breeds.component.SwipeNopeOverlay
import io.posa.feature.breeds.util.getRandomNoSwipeMessage
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

private val SWIPE_THRESHOLD_DP = 110.dp
private const val VELOCITY_THRESHOLD = 700f
private const val VISIBLE_CARDS = 3
private const val BACK_CARD_SCALE_STEP = 0.04f
private const val BACK_CARD_Y_STEP_DP = 18f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BreedsScreen(
    viewModel: BreedsViewModel = koinViewModel(),
    onNavigateToFavourites: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    BreedsScreenContent(
        uiState = uiState,
        events = viewModel.events,
        onNavigateToFavourites = onNavigateToFavourites,
        onSwipeRight = viewModel::swipeRight,
        onSwipeLeft = viewModel::swipeLeft,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
internal fun BreedsScreenContent(
    uiState: BreedsUiState,
    events: SharedFlow<BreedsEvent>,
    onNavigateToFavourites: () -> Unit,
    onSwipeRight: (CatBreed) -> Unit,
    onSwipeLeft: (CatBreed) -> Unit,
    favouriteAddedMessage: String = "Added to favourites ❤️",
    trollMessageProvider: () -> String = ::getRandomNoSwipeMessage,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()

            when (event) {
                is BreedsEvent.FavouriteAdded ->
                    snackbarHostState.showSnackbar(favouriteAddedMessage)

                is BreedsEvent.DismissedButHellNah ->
                    snackbarHostState.showSnackbar(trollMessageProvider())

                is BreedsEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .semantics { testTagsAsResourceId = true }
            .testTag(UiIdentifiers.BREEDS_SCREEN),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BreedsTopBar(onNavigateToFavourites = onNavigateToFavourites)
        },
    ) { _ ->
        BreedsContent(
            uiState = uiState,
            onSwipeRight = onSwipeRight,
            onSwipeLeft = onSwipeLeft,
        )
    }
}

@Composable
private fun BreedsContent(
    uiState: BreedsUiState,
    onSwipeLeft: (CatBreed) -> Unit,
    onSwipeRight: (CatBreed) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(UiIdentifiers.BREEDS_CONTENT),
    ) {
        when {
            uiState.isLoading && uiState.deck.isEmpty() ->
                BreedsLoadingContent()

            uiState.error != null && uiState.deck.isEmpty() ->
                BreedsErrorContent(error = uiState.error!!)

            uiState.deck.isEmpty() ->
                BreedsDeckEmptyContent(reachedEnd = uiState.hasReachedEnd)

            else ->
                BreedsDeckContent(
                    uiState = uiState,
                    onSwipeLeft = onSwipeLeft,
                    onSwipeRight = onSwipeRight,
                )
        }
    }
}

@Composable
private fun BreedsDeckContent(
    uiState: BreedsUiState,
    onSwipeRight: (CatBreed) -> Unit,
    onSwipeLeft: (CatBreed) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val topBreed = uiState.deck.firstOrNull()

    val offsetX = remember(topBreed?.id) { Animatable(0f) }
    val offsetY = remember(topBreed?.id) { Animatable(0f) }

    fun triggerTrollSwipe(breed: CatBreed) {
        scope.launch {
            offsetX.animateTo(2000f, tween(360, easing = FastOutLinearInEasing))
            onSwipeLeft(breed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiIdentifiers.BREEDS_DECK),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = TopAppBarDefaults.TopAppBarExpandedHeight)
                .padding(20.dp),
        ) {
            val visibleBreeds = uiState.deck.take(VISIBLE_CARDS)

            visibleBreeds.reversed().forEachIndexed { reversedIdx, breed ->
                val depthIndex = visibleBreeds.size - 1 - reversedIdx

                key(breed.id) {
                    val cardModifier = Modifier
                        .fillMaxSize()

                    if (depthIndex == 0) {
                        BreedTopCard(
                            breed = breed,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            onSwipeLeft = { triggerTrollSwipe(breed) },
                            onSwipeRight = { onSwipeRight(breed) },
                            modifier = cardModifier,
                        )
                    } else {
                        BreedBackCard(
                            breed = breed,
                            depthIndex = depthIndex,
                            modifier = cardModifier,
                        )
                    }
                }
            }
        }

        if (uiState.isPrefetching) {
            Text(
                text = "Loading more…",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.35f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedsTopBar(
    onNavigateToFavourites: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {
            Text(
                text = "Posa",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
        },
        actions = {
            BadgedBox(
                badge = {},
                modifier = Modifier.padding(end = 4.dp),
            ) {
                IconButton(
                    onClick = onNavigateToFavourites,
                    modifier = Modifier
                        .testTag(UiIdentifiers.BREEDS_TOP_BAR_FAVOURITES_BUTTON)
                        .semantics { role = Role.Button },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.favourite),
                        contentDescription = "Favourites",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        },
    )
}

@Composable
private fun BreedTopCard(
    breed: CatBreed,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val velocityTracker = remember { VelocityTracker() }
    var hasPassedThreshold by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .testTag(UiIdentifiers.breedsCardTop(breed.id))
            .semantics(mergeDescendants = true) {
                contentDescription = "Breed card ${breed.name}"
            }
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = offsetX.value * 0.018f
            }
            .pointerInput(breed.id) {
                detectDragGestures(
                    onDragStart = {
                        velocityTracker.resetTracking()
                        hasPassedThreshold = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                        val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.toPx() }
                        val crossed = abs(offsetX.value) > thresholdPx
                        if (crossed && !hasPassedThreshold) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hasPassedThreshold = true
                        } else if (!crossed) {
                            hasPassedThreshold = false
                        }
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        val thresholdPx = with(density) { SWIPE_THRESHOLD_DP.toPx() }
                        when {
                            offsetX.value > thresholdPx || velocity.x > VELOCITY_THRESHOLD ->
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = 2000f,
                                        animationSpec = tween(360, easing = FastOutLinearInEasing),
                                    )
                                    onSwipeRight()
                                }

                            offsetX.value < -thresholdPx || velocity.x < -VELOCITY_THRESHOLD ->
                                coroutineScope.launch {
                                    offsetX.animateTo(
                                        targetValue = -2000f,
                                        animationSpec = tween(360, easing = FastOutLinearInEasing),
                                    )
                                    onSwipeLeft()
                                }

                            else ->
                                coroutineScope.launch {
                                    launch {
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                        )
                                    }
                                    launch {
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                        )
                                    }
                                }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            launch { offsetX.animateTo(0f, spring()) }
                            launch { offsetY.animateTo(0f, spring()) }
                        }
                    },
                )
            },
    ) {
        BreedCardContent(breed = breed)

        SwipeLikeOverlay(offsetX = offsetX)
        SwipeNopeOverlay(offsetX = offsetX)
    }
}

@Composable
private fun BreedBackCard(
    breed: CatBreed,
    depthIndex: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .testTag(UiIdentifiers.breedsCardBack(breed.id))
            .semantics(mergeDescendants = true) {
                contentDescription = "Breed card ${breed.name}"
            }
            .graphicsLayer {
                val scale = 1f - depthIndex * BACK_CARD_SCALE_STEP
                scaleX = scale
                scaleY = scale
                translationY = depthIndex * BACK_CARD_Y_STEP_DP.dp.toPx()
            },
    ) {
        BreedCardContent(breed = breed, isInteractive = false)
    }
}

@Composable
internal fun BreedCardContent(
    breed: CatBreed,
    isInteractive: Boolean = true,
) {
    val elevationDp by animateDpAsState(
        targetValue = if (isInteractive) 20.dp else 6.dp,
        animationSpec = tween(300, easing = FastOutLinearInEasing),
    )

    val overlay = MaterialTheme.colorScheme.onSurface

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = elevationDp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = breed.imageUrl,
                contentDescription = breed.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.8f to overlay.copy(alpha = 0.7f),
                                1.00f to overlay.copy(alpha = 1f),
                            )
                        )
                    },
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BreedNameRow(breed = breed)
                TemperamentChips(temperaments = breed.temperaments.take(3))
                BreedTraitBars(traits = breed.traits)
                BreedBadgesRow(badges = breed.badges)
            }
        }
    }
}

@Composable
private fun BreedNameRow(breed: CatBreed) {
    val secondaryTextColor = LocalContentColor.current.copy(alpha = 0.75f)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = breed.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (!breed.altName.isNullOrBlank()) {
                Text(
                    text = breed.altName!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryTextColor,
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = breed.origin,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = secondaryTextColor,
            )
            Text(
                text = "🕑 ${breed.lifeSpan} yrs",
                style = MaterialTheme.typography.labelSmall,
                color = secondaryTextColor,
            )
        }
    }
}

@Composable
private fun TemperamentChips(temperaments: List<String>) {
    if (temperaments.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        temperaments.forEach { label ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BreedTraitBars(traits: CatTraits) {
    val displayTraits = remember(traits) {
        listOf(
            "Affection" to traits.affectionLevel,
            "Energy" to traits.energyLevel,
            "Grooming" to traits.grooming,
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        displayTraits.forEach { (label, level) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(60.dp),
                )

                LinearProgressIndicator(
                    progress = { level / 5f },
                    drawStopIndicator = {},
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp),
                )
            }
        }
    }
}

@Composable
private fun BreedBadgesRow(badges: CatBadges) {
    val activeBadges = remember(badges) {
        buildList {
            if (badges.isIndoor) add("🏠 Indoor")
            if (badges.isHypoallergenic) add("🌿 Hypo")
            if (badges.isHairless) add("✨ Hairless")
            if (badges.hasShortLegs) add("🐾 Shorties")
            if (badges.isLap) add("❤️ Lap")
        }
    }
    if (activeBadges.isEmpty()) return

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        activeBadges.forEach { badge ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.25f)),
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}