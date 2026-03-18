package io.posa.data.repository

import io.posa.core.common.enum.SortOrder
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.repository.CatBreedRepository

class CatBreedRepositoryImpl(
    private val remote: CatBreedDataSource,
    private val local: CatBreedDataSource
) : CatBreedRepository {
    override suspend fun getBreeds(
        page: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<CatBreed> {
        return remote.getBreeds(
            page = page,
            limit = limit,
            sortOrder = sortOrder,
        )
    }

    override suspend fun getBreed(id: String): CatBreed? {
        return when {
            local.getBreed(id) != null -> local.getBreed(id)
            else -> remote.getBreed(id)
        }
    }

    override suspend fun insert(breed: CatBreed) {
        local.insert(breed)
    }

    override suspend fun delete(breed: CatBreed) {
        local.delete(breed)
    }
}