package io.posa.data.datasource.favourite

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.posa.core.common.enum.SortOrder
import io.posa.core.datastore.PosaDataStore
import io.posa.domain.datasource.FavouriteImagesDataSource
import io.posa.domain.model.favourite.FavouriteImage
import io.pusa.network.TheCatApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class RemoteFavouriteImagesDataSource(
    private val api: TheCatApiService,
    private val dataStore: DataStore<Preferences>
) : FavouriteImagesDataSource {
    override fun getFavourites(
        page: Int,
        limit: Int,
        sortOrder: SortOrder,
    ): Flow<List<FavouriteImage>> {
        return dataStore.data
            .flatMapLatest { prefs ->
                val userId = prefs[PosaDataStore.PREF_USER_ID]
                if (userId == null) {
                return@flatMapLatest flowOf(emptyList<FavouriteImage>())
                }

                api.getFavourites(
                    userId = userId,
                    page = page,
                    limit = limit,
                    order = if (sortOrder.isDescending) "DESC" else "ASC"
                ).map { list ->
                    list.map { dto -> dto.toDomain() }
                }
            }
    }

    override suspend fun addFavourite(data: FavouriteImage) {
        val userId = requireUserId(action = "add")

        api.addFavourite(
            imageId = data.imageId,
            userId = userId
        )
    }

    override suspend fun removeFavourite(id: Long) {
        requireUserId(action = "remove")

        api.removeFavourite(id = id)
    }

    private suspend fun requireUserId(action: String): String {
        val userId = dataStore.data.first()[PosaDataStore.PREF_USER_ID]
        requireNotNull(userId) {
            "User ID must be set in DataStore to $action a favourite image."
        }
        return userId
    }
}