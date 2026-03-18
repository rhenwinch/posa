package io.posa.domain.usecase

import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GetFavourites(
    private val repository: FavouriteImageRepository
) {
    companion object {
        val log = Logger.withTag("GetFavourites")
    }

    operator fun invoke(page: Int, sortOrder: SortOrder): Flow<Async<List<FavouriteImage>>> {
        val flow: Flow<Async<List<FavouriteImage>>> = repository.getFavouriteImages(
            page = page,
            sortOrder = sortOrder
        ).map {
            Async.Success(it)
        }

        return flow
            .onStart { emit(Async.Loading) }
            .catch { error ->
                log.e(error) {
                    "Failed to get favourite images for page $page and sort order $sortOrder"
                }

                emit(Async.Fail(error))
            }
    }
}