package io.posa.domain.usecase

import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.model.image.CatImage
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.flow

class RemoveFromFavourites(
    private val repository: FavouriteImageRepository
) {
    companion object {
        val log = Logger.withTag("RemoveFromFavourites")
    }

    operator fun invoke(favourite: FavouriteImage) = flow<Async<Unit>> {
        emit(Async.Loading)
        try {
            repository.removeFavouriteImage(favourite)
            emit(Async.Success(Unit))
        } catch (e: Exception) {
            log.e(e) { "Failed to remove image ${favourite.imageId} from favourites" }
            emit(Async.Fail(e))
        }
    }
}