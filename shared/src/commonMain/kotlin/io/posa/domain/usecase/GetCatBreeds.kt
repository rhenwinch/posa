package io.posa.domain.usecase

import co.touchlab.kermit.Logger
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.repository.CatBreedRepository
import kotlinx.coroutines.flow.flow

class GetCatBreeds(
    private val repository: CatBreedRepository
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
            val breeds = repository.getBreeds(
                page = page,
                sortOrder = sortOrder
            ).filterNot {
                val isDuplicate = it.id in shownBreeds
                if (!isDuplicate) {
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
}