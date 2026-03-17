package io.posa.domain.datasource

import io.posa.core.common.enum.Order

interface FavouriteImagesDataSource {
    suspend fun getFavourites(
        page: Int,
        limit: Int,
        order: Order,
    ): List<String>

    suspend fun addFavourite(imageId: String)

    suspend fun removeFavourite(id: String)
}