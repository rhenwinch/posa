package io.posa.feature.favourites

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.posa.R
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.feature.favourites.component.EndOfListLabel
import io.posa.feature.favourites.component.FavouriteCard
import io.posa.feature.favourites.component.FavouritesEmptyContent
import io.posa.feature.favourites.component.FavouritesErrorContent
import io.posa.feature.favourites.component.FavouritesLoadingContent
import io.posa.feature.favourites.component.PaginationFooter
import io.posa.feature.favourites.component.SortOrderToggle
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel

private const val PAGINATION_PREFETCH_DISTANCE = 4

@Composable
internal fun FavouritesScreen(
    viewModel: FavouritesViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                FavouritesEvent.FavouriteRemoved ->
                    snackbarHostState.showSnackbar("🤨🤨🤨⁉️")

                is FavouritesEvent.ShowError ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
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
            onRemoveCard = viewModel::removeCard,
            onLoadNextPage = viewModel::loadNextPage,
            onSortOrderChange = viewModel::onSortOrderChange,
            modifier = Modifier.padding(innerPadding),
        )
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
            IconButton(onClick = onNavigateBack) {
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
    onLoadNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    onSortOrderChange: (SortOrder) -> Unit,
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
                    onLoadNextPage = onLoadNextPage,
                    onSortOrderChange = onSortOrderChange
                )
        }
    }
}

@Composable
private fun FavouritesGridContent(
    uiState: FavouritesUiState,
    onRemoveCard: (FavouriteImage) -> Unit,
    onLoadNextPage: () -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            lastVisible to total
        }
            .distinctUntilChanged()
            .filter { (lastVisible, total) ->
                total > 0 && lastVisible >= total - PAGINATION_PREFETCH_DISTANCE
            }
            .collect { onLoadNextPage() }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 24.dp,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SortOrderToggle(
                sortOrder = uiState.sortOrder,
                onSortOrderChange = onSortOrderChange,
            )
        }

        items(
            items = uiState.favourites,
            key = { it.imageId },
        ) { favourite ->
            FavouriteCard(
                favourite = favourite,
                onRemove = { onRemoveCard(favourite) },
                modifier = Modifier.animateItem(),
            )
        }

        if (uiState.isPaginating) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PaginationFooter()
            }
        }

        if (uiState.hasReachedEnd && uiState.favourites.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EndOfListLabel()
            }
        }
    }
}