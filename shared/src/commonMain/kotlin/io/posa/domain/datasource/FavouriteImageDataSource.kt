package io.posa.domain.datasource

import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.flow.Flow

interface FavouriteImageDataSource {
    fun getFavourites(
        page: Int,
        limit: Int,
        sortOrder: SortOrder,
    ): Flow<List<FavouriteImage>>

    suspend fun addFavourite(data: FavouriteImage): Long

    suspend fun getPendingSyncFavourites(): List<FavouriteImage>

    suspend fun removeFavourite(id: Long)
}