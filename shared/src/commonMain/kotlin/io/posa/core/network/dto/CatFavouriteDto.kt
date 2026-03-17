package io.pusa.network.dto

import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.model.sync.SyncStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class CatFavouriteDto(
    val id: Long,
    val image: CatImageDto,
    @SerialName("created_at") val createdAt: Instant,
    @SerialName("image_id") val imageId: String,
    @SerialName("sub_id") val subId: String,
    @SerialName("user_id") val userId: String,
) {
    fun toDomain() = FavouriteImage(
        id = id,
        imageId = imageId,
        imageUrl = image.url,
        breedName = image.breeds.first().name,
        createdAt = createdAt,
        syncStatus = SyncStatus.SYNCED,
    )
}