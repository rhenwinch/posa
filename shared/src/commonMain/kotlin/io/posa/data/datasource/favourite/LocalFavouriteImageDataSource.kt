package io.posa.data.datasource.favourite

import io.posa.core.common.enum.SortOrder
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.domain.datasource.FavouriteImageDataSource
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class LocalFavouriteImageDataSource(
    private val favouritesDao: FavouriteImageDao,
    private val breedsDao: CatBreedDao,
) : FavouriteImageDataSource {
    override fun getFavourites(
        page: Int,
        limit: Int,
        sortOrder: SortOrder,
    ): Flow<List<FavouriteImage>> {
        val listFlow = if (sortOrder.isDescending) {
            favouritesDao.getAllDescAsFlow(
                page = page,
                limit = limit,
            )
        } else {
            favouritesDao.getAllAscAsFlow(
                page = page,
                limit = limit,
            )
        }

        return listFlow.mapLatest { list ->
            list.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun addFavourite(data: FavouriteImage): Long {
        return favouritesDao.add(FavouriteImageEntity.from(data))
    }

    override suspend fun getPendingSyncFavourites(): List<FavouriteImage> {
        return favouritesDao.getAllPendingSync().map { it.toDomain() }
    }

    override suspend fun removeFavourite(id: Long) {
        favouritesDao.remove(id)
    }
}