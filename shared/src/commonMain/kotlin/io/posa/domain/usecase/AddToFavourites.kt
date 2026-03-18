package io.posa.domain.usecase

import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.model.image.CatImage
import io.posa.domain.repository.CatBreedRepository
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.flow

class AddToFavourites(
    private val favouriteRepository: FavouriteImageRepository,
    private val catBreedRepository: CatBreedRepository
) {
    companion object {
        val log = Logger.withTag("AddToFavourites")
    }

    operator fun invoke(image: CatImage) = flow<Async<Unit>> {
        emit(Async.Loading)
        try {
            val favouriteImage = FavouriteImage.from(image)
            catBreedRepository.insert(image.breed)
            favouriteRepository.addFavouriteImage(favouriteImage)
            emit(Async.Success(Unit))
        } catch (e: Exception) {
            log.e(e) { "Failed to add image ${image.id} to favourites" }
            emit(Async.Fail(e))
        }
    }
}