package io.posa.domain.datasource

import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.flow.Flow

interface FavouriteImagesDataSource {
    fun getFavourites(
        page: Int,
        limit: Int,
        sortOrder: SortOrder,
    ): Flow<List<FavouriteImage>>

    suspend fun addFavourite(data: FavouriteImage)

    suspend fun removeFavourite(id: Long)
}