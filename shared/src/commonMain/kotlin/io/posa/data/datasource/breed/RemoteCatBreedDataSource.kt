package io.posa.data.datasource.breed

import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastMap
import co.touchlab.kermit.Logger
import io.posa.core.common.AppDispatchers
import io.posa.core.common.enum.SortOrder
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import io.posa.core.network.TheCatApiService
import kotlinx.coroutines.withContext

class RemoteCatBreedDataSource(
    private val apiService: TheCatApiService
) : CatBreedDataSource {
    companion object {
        const val QUALIFIER_NAME = "RemoteCatBreedDataSource"
    }

    override suspend fun getBreeds(
        page: Int,
        limit: Int,
        sortOrder: SortOrder
    ): List<CatBreed> {
        return withContext(AppDispatchers.IO) {
            apiService.getCatImages(
                page = page,
                limit = limit,
                order = when (sortOrder) {
                    SortOrder.RANDOM -> "RANDOM"
                    SortOrder.ASC -> "ASC"
                    SortOrder.DESC -> "DESC"
                }
            ).fastFlatMap { images ->
                images.breeds
                    .fastMap { breed ->
                        if (breed.referenceImageId == null) {
                            breed.copy(referenceImageId = images.id)
                        } else {
                            breed
                        }.toDomain()
                    }
            }
        }
    }

    override suspend fun getBreed(id: String): CatBreed {
        return withContext(AppDispatchers.IO) {
            apiService.getBreed(id).toDomain()
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