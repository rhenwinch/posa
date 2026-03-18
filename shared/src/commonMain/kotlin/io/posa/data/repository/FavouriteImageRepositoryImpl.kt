package io.posa.data.repository

import co.touchlab.kermit.Logger
import io.posa.core.common.enum.SortOrder
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.datasource.FavouriteImageDataSource
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FavouriteImageRepositoryImpl(
    private val remote: FavouriteImageDataSource,
    private val local: FavouriteImageDataSource
) : FavouriteImageRepository {
    private companion object {
        val log = Logger.withTag("FavouriteImageRepositoryImpl")
    }

    override fun getFavouriteImages(
        page: Int,
        limit: Int,
        sortOrder: SortOrder
    ): Flow<List<FavouriteImage>> {
        return local.getFavourites(
            page = page,
            limit = limit,
            sortOrder = sortOrder
        )
    }

    override suspend fun addFavouriteImage(image: FavouriteImage) {
        try {
            val remoteId = remote.addFavourite(image)
            local.addFavourite(
                image.copy(
                    id = remoteId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        } catch (e: Exception) {
            log.w(e) {
                "Failed to add favourite image to remote. Will retry during synchronization..."
            }
        }
    }

    override suspend fun removeFavouriteImage(image: FavouriteImage) {
        local.removeFavourite(image.id)

        coroutineScope {
            launch {
                try {
                    remote.removeFavourite(image.id)
                } catch (e: Exception) {
                    local.addFavourite(image.copy(syncStatus = SyncStatus.PENDING_DELETE))
                    log.w(e) {
                        "Failed to remove favourite image to remote. Will retry during synchronization..."
                    }
                }
            }
        }
    }

    override suspend fun synchronize() {
        val pendingSyncItems = local.getPendingSyncFavourites()

        pendingSyncItems.forEach { item ->
            when (item.syncStatus) {
                SyncStatus.PENDING_SYNC -> {
                    try {
                        remote.addFavourite(item)
                        local.addFavourite(item.copy(syncStatus = SyncStatus.SYNCED))
                    } catch (e: Exception) {
                        log.w(e) {
                            "Failed to synchronize pending favourite image addition to remote. Will retry during next synchronization..."
                        }
                    }
                }
                SyncStatus.PENDING_DELETE -> {
                    try {
                        remote.removeFavourite(item.id)
                        local.removeFavourite(item.id)
                    } catch (e: Exception) {
                        log.w(e) {
                            "Failed to synchronize pending favourite image deletion to remote. Will retry during next synchronization..."
                        }
                    }
                }
                else -> { /* No action needed for SYNCED items */ }
            }
        }
    }
}