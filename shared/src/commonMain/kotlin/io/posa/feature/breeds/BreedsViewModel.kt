package io.posa.feature.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.image.CatImage
import io.posa.domain.usecase.AddToFavourites
import io.posa.domain.usecase.GetCatBreeds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BreedsUiState(
    val deck: List<CatBreed> = emptyList(),
    val isLoading: Boolean = false,
    val isPrefetching: Boolean = false,
    val error: Throwable? = null,
    val sortOrder: SortOrder = SortOrder.RANDOM,
    val hasReachedEnd: Boolean = false,
)

sealed interface BreedsEvent {
    data object FavouriteAdded : BreedsEvent
    data class ShowError(val message: String) : BreedsEvent
}

class BreedsViewModel(
    private val getCatBreeds: GetCatBreeds,
    private val addToFavourites: AddToFavourites,
) : ViewModel() {

    companion object {
        private val log = Logger.withTag("BreedsViewModel")
        const val PAGE_SIZE = 10

        const val PRELOAD_THRESHOLD = 3
    }

    private var currentPage = 0

    private val _uiState = MutableStateFlow(BreedsUiState())
    val uiState: StateFlow<BreedsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BreedsEvent>()
    val events: SharedFlow<BreedsEvent> = _events.asSharedFlow()

    init {
        fetchBreeds(isRefresh = true)
    }

    fun swipeRight(breed: CatBreed) {
        removeFromDeck(breed)
        val image = CatImage(
            id = breed.id,
            url = breed.imageUrl,
            breed = breed,
        )
        viewModelScope.launch {
            addToFavourites(image).collect { async ->
                when (async) {
                    Async.Loading -> Unit
                    is Async.Success -> _events.emit(BreedsEvent.FavouriteAdded)
                    is Async.Fail -> {
                        log.e(async.error) { "Failed to favourite breed ${breed.id}" }
                        _events.emit(BreedsEvent.ShowError(async.error.message ?: "Unknown error"))
                    }
                    else -> {/* No-op */}
                }
            }
        }
    }

    /**
     * Called when the user swipes left (pass). Simply removes the card from the deck.
     */
    fun swipeLeft(breed: CatBreed) {
        removeFromDeck(breed)
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        if (_uiState.value.sortOrder == sortOrder) return
        _uiState.update {
            it.copy(
                sortOrder = sortOrder,
                deck = emptyList(),
                hasReachedEnd = false,
                error = null,
            )
        }
        fetchBreeds(isRefresh = true)
    }

    private fun removeFromDeck(breed: CatBreed) {
        _uiState.update { it.copy(deck = it.deck.filterNot { b -> b.id == breed.id }) }
        checkAndPreload()
    }

    private fun checkAndPreload() {
        val state = _uiState.value
        if (state.deck.size <= PRELOAD_THRESHOLD
            && !state.isPrefetching
            && !state.isLoading
            && !state.hasReachedEnd
        ) {
            fetchBreeds(isRefresh = false)
        }
    }

    private fun fetchBreeds(isRefresh: Boolean) {
        if (isRefresh) {
            currentPage = 0
        }

        viewModelScope.launch {
            val page = currentPage
            val sortOrder = _uiState.value.sortOrder

            getCatBreeds(page = page, sortOrder = sortOrder).collect { async ->
                when (async) {
                    Async.Loading -> _uiState.update {
                        if (isRefresh) it.copy(isLoading = true, error = null)
                        else it.copy(isPrefetching = true, error = null)
                    }

                    is Async.Success -> {
                        val newBreeds = async()
                        currentPage++
                        _uiState.update {
                            it.copy(
                                deck = it.deck + newBreeds,
                                isLoading = false,
                                isPrefetching = false,
                                hasReachedEnd = newBreeds.size < PAGE_SIZE,
                                error = null,
                            )
                        }
                    }

                    is Async.Fail -> {
                        log.e(async.error) {
                            "Failed to load breeds [page=$page, sortOrder=$sortOrder]"
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isPrefetching = false,
                                error = async.error,
                            )
                        }
                    }

                    else -> {/* No-op */}
                }
            }
        }
    }
}