package io.posa.data.datasource.breed

import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import io.pusa.network.TheCatApiService

class RemoteCatBreedDataSource(
    private val apiService: TheCatApiService
) : CatBreedDataSource {
    companion object {
        const val QUALIFIER_NAME = "RemoteCatBreedDataSource"
    }

    override suspend fun getBreed(id: String): CatBreed {
        TODO("Not yet implemented")
    }
}