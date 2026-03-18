package io.posa.data.datasource.favourite

import io.posa.core.common.enum.SortOrder
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.domain.datasource.FavouriteImagesDataSource
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class LocalFavouriteImagesDataSource(
    private val dao: FavouriteImageDao
) : FavouriteImagesDataSource {
    override fun getFavourites(
        page: Int,
        limit: Int,
        sortOrder: SortOrder,
    ): Flow<List<FavouriteImage>> {
        val listFlow = if (sortOrder.isDescending) {
            dao.getAllDescAsFlow(
                page = page,
                limit = limit,
            )
        } else {
            dao.getAllAscAsFlow(
                page = page,
                limit = limit,
            )
        }

        return listFlow.mapLatest { list ->
            list.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun addFavourite(data: FavouriteImage) {
        dao.add(FavouriteImageEntity.from(data))
    }

    override suspend fun removeFavourite(id: Long) {
        dao.remove(id)
    }
}