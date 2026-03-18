package io.posa.domain.datasource

import io.posa.domain.model.breed.CatBreed
import org.koin.core.qualifier.Qualifier

interface CatBreedDataSource {
    suspend fun insert(breed: CatBreed)

    suspend fun delete(id: String)

    suspend fun delete(breed: CatBreed)

    suspend fun getBreed(id: String): CatBreed?
}