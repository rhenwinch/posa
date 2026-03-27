package io.posa.test

import io.posa.core.common.enum.SyncStatus
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage

object TestFakes {
    fun sampleBreed(
        id: String,
        name: String = "Abyssinian",
        imageId: String = "0XYvRd7oD",
    ): CatBreed {
        return CatBreed(
            id = id,
            name = name,
            altName = "Abys",
            imageId = imageId,
            origin = "Egypt",
            description = "desc",
            lifeSpan = "14 - 15",
            weight = "3 - 5",
            temperaments = listOf("Independent", "Active"),
            traits = CatTraits(
                adaptability = 5,
                affectionLevel = 5,
                childFriendly = 3,
                dogFriendly = 4,
                energyLevel = 5,
                grooming = 1,
                healthIssues = 2,
                intelligence = 5,
                sheddingLevel = 2,
                socialNeeds = 4,
                strangerFriendly = 4,
                vocalisation = 1,
            ),
            badges = CatBadges(
                isLap = true,
                isHypoallergenic = false,
                isHairless = false,
                hasShortLegs = false,
                isIndoor = true,
            ),
        )
    }

    fun sampleFavourite(
        imageId: String,
        breedId: String = "breed_$imageId",
        url: String = "https://example.com/$imageId.jpg",
    ): FavouriteImage {
        return FavouriteImage(
            id = 1L,
            imageId = imageId,
            imageUrl = url,
            createdAt = 0L,
            breed = sampleBreed(id = breedId),
            syncStatus = SyncStatus.SYNCED,
        )
    }
}
