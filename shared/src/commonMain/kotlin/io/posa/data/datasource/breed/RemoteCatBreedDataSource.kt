package io.posa.data.datasource.breed

import co.touchlab.kermit.Logger
import io.posa.core.common.AppDispatchers
import io.posa.core.common.enum.Measurement
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import io.pusa.network.TheCatApiService
import kotlinx.coroutines.withContext

class RemoteCatBreedDataSource(
    private val apiService: TheCatApiService
) : CatBreedDataSource {
    companion object {
        private val log = Logger.withTag(QUALIFIER_NAME)

        const val QUALIFIER_NAME = "RemoteCatBreedDataSource"
    }

    override suspend fun getBreed(id: String): CatBreed? {
        return withContext(AppDispatchers.IO) {
            apiService.getBreed(id).toDomain(
                // TODO: Allow user to choose between imperial and metric units for weight and height
                measurement = Measurement.IMPERIAL
            )
        }
    }

    override suspend fun insert(breed: CatBreed) {
        throw UnsupportedOperationException("Cannot insert breed into remote data source")
    }

    override suspend fun delete(id: String) {
        throw UnsupportedOperationException("Cannot delete breed from remote data source")
    }

    override suspend fun delete(breed: CatBreed) {
        throw UnsupportedOperationException("Cannot delete breed from remote data source")
    }
}