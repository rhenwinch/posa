package io.posa.feature.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.posa.R
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.feature.favourites.component.BreedDetailSheet
import io.posa.feature.favourites.component.EndOfListLabel
import io.posa.feature.favourites.component.FavouriteCard
import io.posa.feature.favourites.component.FavouritesEmptyContent
import io.posa.feature.favourites.component.FavouritesErrorContent
import io.posa.feature.favourites.component.FavouritesLoadingContent
import io.posa.feature.favourites.component.SortOrderToggle
import kotlinx.coroutines.flow.SharedFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun FavouritesScreen(
    viewModel: FavouritesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    FavouritesScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRemoveCard = viewModel::remove,
        onSortOrderChange = viewModel::onSortOrderChange,
        events = viewModel.events,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesScreenContent(
    uiState: FavouritesUiState,
    onNavigateBack: () -> Unit,
    onRemoveCard: (FavouriteImage) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    events: SharedFlow<FavouritesEvent>,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var clickedBreed by remember { mutableStateOf<CatBreed?>(null) }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                FavouritesEvent.FavouriteRemoved ->
                    snackbarHostState.showSnackbar("🤨🤨🤨⁉️")

                is FavouritesEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .semantics { testTagsAsResourceId = true }
            .testTag("favourites:screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FavouritesTopBar(
                count = uiState.favourites.size,
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        FavouritesContent(
            uiState = uiState,
            onRemoveCard = onRemoveCard,
            onSortOrderChange = onSortOrderChange,
            onViewCard = { clickedBreed = it.breed },
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (clickedBreed != null) {
        ModalBottomSheet(
            onDismissRequest = { clickedBreed = null },
            modifier = Modifier.testTag("favourites:detailSheet"),
        ) {
            BreedDetailSheet(
                breed = clickedBreed!!,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesTopBar(
    count: Int,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .testTag("favourites:topBar:backButton")
                    .semantics { role = Role.Button },
            ) {
                Icon(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back",
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Favourites",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                if (count > 0) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalTextStyle.current.color.copy(alpha = 0.6f),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainerLow, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun FavouritesContent(
    uiState: FavouritesUiState,
    onRemoveCard: (FavouriteImage) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewCard: (FavouriteImage) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.favourites.isEmpty() ->
                FavouritesLoadingContent()

            uiState.error != null && uiState.favourites.isEmpty() ->
                FavouritesErrorContent(error = uiState.error!!)

            uiState.favourites.isEmpty() ->
                FavouritesEmptyContent()

            else ->
                FavouritesGridContent(
                    uiState = uiState,
                    onRemoveCard = onRemoveCard,
                    onSortOrderChange = onSortOrderChange,
                    onViewCard = onViewCard,
                )
        }
    }
}

@Composable
private fun FavouritesGridContent(
    uiState: FavouritesUiState,
    onRemoveCard: (FavouriteImage) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onViewCard: (FavouriteImage) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("favourites:grid"),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SortOrderToggle(
                sortOrder = uiState.sortOrder,
                onSortOrderChange = onSortOrderChange,
                modifier = Modifier.testTag("favourites:sortOrder"),
            )
        }

        items(
            items = uiState.favourites,
            key = { it.imageId },
        ) { favourite ->
            FavouriteCard(
                favourite = favourite,
                onRemove = { onRemoveCard(favourite) },
                modifier = Modifier
                    .animateItem()
                    .testTag("favourites:item:${favourite.imageId}")
                    .semantics { role = Role.Button }
                    .clickable {
                        onViewCard(favourite)
                    },
            )
        }

        if (uiState.favourites.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EndOfListLabel()
            }
        }
    }
}