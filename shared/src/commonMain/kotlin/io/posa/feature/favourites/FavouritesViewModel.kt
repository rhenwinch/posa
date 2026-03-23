package io.posa.feature.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.usecase.GetFavourites
import io.posa.domain.usecase.RemoveFromFavourites
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class FavouritesUiState(
    val favourites: List<FavouriteImage> = emptyList(),
    val isLoading: Boolean = false,
    val isPaginating: Boolean = false,
    val error: Throwable? = null,
    val sortOrder: SortOrder = SortOrder.DESC,
    val hasReachedEnd: Boolean = false,
)

internal sealed interface FavouritesEvent {
    data object FavouriteRemoved : FavouritesEvent
    data class ShowError(val message: String) : FavouritesEvent
}

internal class FavouritesViewModel(
    private val getFavourites: GetFavourites,
    private val removeFromFavourites: RemoveFromFavourites,
) : ViewModel() {

    companion object {
        private val log = Logger.withTag("FavouritesViewModel")
        const val PAGE_SIZE = 10
    }

    private var currentPage = 0

    private val _uiState = MutableStateFlow(FavouritesUiState())
    val uiState: StateFlow<FavouritesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FavouritesEvent>()
    val events: SharedFlow<FavouritesEvent> = _events.asSharedFlow()

    init {
        fetchFavourites(isRefresh = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isPaginating || state.hasReachedEnd) return
        fetchFavourites(isRefresh = false)
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        if (_uiState.value.sortOrder == sortOrder) return
        _uiState.update {
            it.copy(
                sortOrder = sortOrder,
                favourites = emptyList(),
                hasReachedEnd = false,
                error = null,
            )
        }
        fetchFavourites(isRefresh = true)
    }

    fun removeCard(favourite: FavouriteImage) {
        val snapshot = _uiState.value.favourites
        val originalIndex = snapshot.indexOfFirst { it.imageId == favourite.imageId }
        if (originalIndex == -1) return

        _uiState.update {
            it.copy(favourites = it.favourites.filterNot { f -> f.imageId == favourite.imageId })
        }

        viewModelScope.launch {
            removeFromFavourites(favourite).collect { async ->
                when (async) {
                    Async.Loading -> Unit
                    is Async.Success -> _events.emit(FavouritesEvent.FavouriteRemoved)
                    is Async.Fail -> {
                        log.e(async.error) {
                            "Failed to remove favourite ${favourite.imageId}"
                        }
                        _uiState.update { state ->
                            val mutable = state.favourites.toMutableList()
                            mutable.add(originalIndex.coerceAtMost(mutable.size), favourite)
                            state.copy(favourites = mutable)
                        }
                        _events.emit(FavouritesEvent.ShowError(async.error.message ?: "Unknown error"))
                    }

                    else -> {/* No-op */}
                }
            }
        }
    }

    private fun fetchFavourites(isRefresh: Boolean) {
        if (isRefresh) currentPage = 0

        viewModelScope.launch {
            val page = currentPage
            val sortOrder = _uiState.value.sortOrder

            val result = getFavourites(page = page, sortOrder = sortOrder)
                .onEach { async ->
                    if (async is Async.Loading) {
                        _uiState.update {
                            if (isRefresh) it.copy(isLoading = true, error = null)
                            else it.copy(isPaginating = true, error = null)
                        }
                    }
                }
                .first { it !is Async.Loading }

            when (result) {
                is Async.Success -> {
                    val newFavourites = result()
                    currentPage++
                    _uiState.update {
                        it.copy(
                            favourites = if (isRefresh) newFavourites
                            else it.favourites + newFavourites,
                            isLoading = false,
                            isPaginating = false,
                            hasReachedEnd = newFavourites.size < PAGE_SIZE,
                            error = null,
                        )
                    }
                }

                is Async.Fail -> {
                    log.e(result.error) {
                        "Failed to load favourites [page=$page, sortOrder=$sortOrder]"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isPaginating = false,
                            error = result.error,
                        )
                    }
                }
                Async.Loading -> Unit
                else -> {/* No-op */}
            }
        }
    }
}