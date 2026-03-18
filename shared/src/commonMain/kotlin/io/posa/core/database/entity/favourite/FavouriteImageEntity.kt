package io.posa.core.database.entity.favourite

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.model.image.CatImage
import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel
import kotlin.time.Instant

@Entity(
    tableName = "favourite_images",
    foreignKeys = [
        ForeignKey(
            entity = CatBreedEntity::class,
            parentColumns = ["id"],
            childColumns = ["breedId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["breedId"])]
)
data class FavouriteImageEntity(
    @PrimaryKey val id: Long,
    val breedId: String,
    val imageId: String,
    val imageUrl: String,
    val breedName: String,
    val createdAt: Long,
    override val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
) : SyncableModel {
    companion object {
        fun from(
            favourite: FavouriteImage,
            breedId: String,
        ): FavouriteImageEntity {
            return FavouriteImageEntity(
                breedId = breedId,
                id = favourite.id,
                imageId = favourite.imageId,
                imageUrl = favourite.imageUrl,
                breedName = favourite.breedName,
                syncStatus = favourite.syncStatus,
                createdAt = favourite.createdAt.toEpochMilliseconds()
            )
        }
    }

    fun toDomain() = FavouriteImage(
        id = id,
        imageId = imageId,
        imageUrl = imageUrl,
        breedName = breedName,
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        syncStatus = syncStatus
    )
}