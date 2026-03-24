package io.posa.feature.favourites

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.usecase.GetFavourites
import io.posa.domain.usecase.RemoveFromFavourites
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
data class FavouritesUiState(
    val favourites: List<FavouriteImage> = emptyList(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val sortOrder: SortOrder = SortOrder.DESC,
)


sealed interface FavouritesEvent {
    @Stable
    data object FavouriteRemoved : FavouritesEvent

    @Stable
    data class ShowError(val message: String) : FavouritesEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModel(
    private val getFavourites: GetFavourites,
    private val removeFromFavourites: RemoveFromFavourites,
) : ViewModel() {

    companion object {
        private val log = Logger.withTag("FavouritesViewModel")
    }

    private val _uiState = MutableStateFlow(FavouritesUiState())

    @NativeCoroutines
    val uiState: StateFlow<FavouritesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FavouritesEvent>()

    @NativeCoroutines
    val events: SharedFlow<FavouritesEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            _uiState.map { it.sortOrder }
                .distinctUntilChanged()
                .flatMapLatest { sort ->
                    getFavourites(sort)
                }.collect { async ->
                    when (async) {
                        Async.Loading -> _uiState.update { state ->
                            if (state.favourites.isEmpty()) {
                                state.copy(isLoading = true, error = null)
                            } else {
                                state.copy(error = null)
                            }
                        }

                        is Async.Success<*> -> _uiState.update { state ->
                            val data = async() ?: emptyList()

                            state.copy(
                                favourites = data,
                                isLoading = false,
                                error = null,
                            )
                        }

                        is Async.Fail -> _uiState.update { state ->
                            state.copy(isLoading = false, error = async.error)
                        }

                        else -> {/* No-op */}
                    }
                }
        }
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        if (_uiState.value.sortOrder == sortOrder) return
        _uiState.update {
            it.copy(
                sortOrder = sortOrder,
                favourites = emptyList(),
                error = null,
            )
        }
    }

    fun remove(favourite: FavouriteImage) {
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
                            state.copy(favourites = mutable.toList())
                        }
                        _events.emit(
                            FavouritesEvent.ShowError(
                                async.error.message ?: "Unknown error"
                            )
                        )
                    }

                    else -> {/* No-op */}
                }
            }
        }
    }
}