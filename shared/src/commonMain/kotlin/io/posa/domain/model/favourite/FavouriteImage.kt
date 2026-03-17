package io.posa.domain.model.favourite

import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel
import kotlin.time.Instant

data class FavouriteImage(
    val id: Long,
    val imageId: String,
    val imageUrl: String,
    val breedName: String,
    val createdAt: Instant,
    override val syncStatus: SyncStatus,
) : SyncableModel