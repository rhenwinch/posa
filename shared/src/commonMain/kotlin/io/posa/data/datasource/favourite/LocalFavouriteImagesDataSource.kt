package io.posa.data.datasource.favourite

import io.posa.core.common.enum.Order
import io.posa.domain.datasource.FavouriteImagesDataSource

class LocalFavouriteImagesDataSource : FavouriteImagesDataSource {
    override suspend fun getFavourites(
        page: Int,
        limit: Int,
        order: Order
    ): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun addFavourite(imageId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeFavourite(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun removeAllFavourites() {
        TODO("Not yet implemented")
    }
}