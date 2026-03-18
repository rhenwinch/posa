package io.posa.domain.repository

import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.flow.Flow

interface FavouriteImageRepository {
    fun getFavouriteImages(
        page: Int,
        limit: Int = 10,
        sortOrder: SortOrder = SortOrder.DESC,
    ): Flow<List<FavouriteImage>>

    suspend fun addFavouriteImage(image: FavouriteImage)

    suspend fun removeFavouriteImage(image: FavouriteImage)

    suspend fun synchronize()
}