package io.posa.domain.datasource

import io.posa.domain.model.breed.CatBreed
import org.koin.core.qualifier.Qualifier

interface CatBreedDataSource {
    suspend fun getBreed(id: String): CatBreed
}