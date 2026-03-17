package io.posa.data.datasource.breed

import io.posa.core.database.dao.CatBreedDao
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import org.koin.core.qualifier.Qualifier

class LocalCatBreedDataSource(
    private val catBreedDao: CatBreedDao
) : CatBreedDataSource {
    companion object {
        const val QUALIFIER_NAME = "LocalCatBreedDataSource"
    }

    override suspend fun getBreed(id: String): CatBreed {
        TODO("Not yet implemented")
    }
}