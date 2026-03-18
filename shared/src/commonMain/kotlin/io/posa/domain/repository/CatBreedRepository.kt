package io.posa.domain.repository

import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBreed

interface CatBreedRepository {
    suspend fun getBreeds(
        page: Int,
        limit: Int = 10,
        sortOrder: SortOrder
    ): List<CatBreed>

    suspend fun getBreed(id: String): CatBreed?

    suspend fun insert(breed: CatBreed)

    suspend fun delete(breed: CatBreed)
}