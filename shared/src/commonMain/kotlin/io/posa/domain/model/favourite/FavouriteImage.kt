package io.posa.domain.model.favourite

import io.posa.domain.model.breed.CatBreed
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.model.image.CatImage
import kotlin.time.Clock
import kotlin.time.Instant

data class FavouriteImage(
    val id: Long = 0,
    val imageId: String,
    val imageUrl: String,
    val createdAt: Instant,
    val breed: CatBreed,
    val syncStatus: SyncStatus,
) {
    companion object {
        fun from(image: CatImage): FavouriteImage {
            return FavouriteImage(
                imageId = image.id,
                imageUrl = image.url,
                createdAt = Clock.System.now(),
                breed = image.breed,
                syncStatus = SyncStatus.PENDING_SYNC,
            )
        }
    }
}