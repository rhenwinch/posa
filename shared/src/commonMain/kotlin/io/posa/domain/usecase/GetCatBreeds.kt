package io.posa.domain.usecase

import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.repository.CatBreedRepository
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.flow

class GetCatBreeds(
    private val catBreedRepository: CatBreedRepository,
    private val favouriteImageRepository: FavouriteImageRepository,
) {
    companion object {
        val log = Logger.withTag("GetRandomCatBreeds")
    }

    private val shownBreeds = mutableSetOf<String>()

    operator fun invoke(
        page: Int,
        sortOrder: SortOrder
    ) = flow<Async<List<CatBreed>>> {
        emit(Async.Loading)
        try {
            val breeds = catBreedRepository.getBreeds(
                page = page,
                sortOrder = sortOrder
            ).filterNot {
                val isDuplicate = it.id in shownBreeds
                if (!isDuplicate || isAddedAlready(it.id)) {
                    shownBreeds.add(it.id)
                }

                isDuplicate
            }

            emit(Async.Success(breeds))
        } catch (error: Throwable) {
            log.e(error) {
                "Failed to get cat breeds for page $page and sort order $sortOrder"
            }
            emit(Async.Fail(error))
        }
    }

    private suspend fun isAddedAlready(breedId: String): Boolean {
        return favouriteImageRepository.isFavourite(breedId)
    }
}