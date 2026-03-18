package io.posa.core.database.entity.favourite

import androidx.room.Embedded
import androidx.room.Relation
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.domain.model.favourite.FavouriteImage
import kotlin.time.Instant

data class FavouriteImageWithBreed(
    @Embedded val favouriteImage: FavouriteImageEntity,
    @Relation(
        parentColumn = "breedId",
        entityColumn = "id"
    )
    val breed: CatBreedEntityWithTraitsAndBadges
) {
    fun toDomain() = FavouriteImage(
        id = favouriteImage.id,
        breed = breed.toDomain(),
        imageId = favouriteImage.imageId,
        imageUrl = favouriteImage.imageUrl,
        createdAt = Instant.fromEpochMilliseconds(favouriteImage.createdAt),
        syncStatus = favouriteImage.syncStatus,
    )
}