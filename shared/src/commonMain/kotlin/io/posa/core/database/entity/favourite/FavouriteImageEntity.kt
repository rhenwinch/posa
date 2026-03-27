package io.posa.core.database.entity.favourite

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.posa.core.common.enum.SyncStatus
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.domain.model.favourite.FavouriteImage

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
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val breedId: String,
    val imageId: String,
    val imageUrl: String,
    val createdAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
) {
    companion object {
        fun from(favourite: FavouriteImage): FavouriteImageEntity {
            return FavouriteImageEntity(
                id = favourite.id,
                breedId = favourite.breed.id,
                imageId = favourite.imageId,
                imageUrl = favourite.imageUrl,
                syncStatus = favourite.syncStatus,
                createdAt = favourite.createdAt
            )
        }
    }
}