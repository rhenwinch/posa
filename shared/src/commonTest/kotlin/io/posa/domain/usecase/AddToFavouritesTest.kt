package io.posa.domain.usecase

import app.cash.turbine.test
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.model.image.CatImage
import io.posa.domain.repository.CatBreedRepository
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddToFavouritesTest {

    @Test
    fun invoke_emitsLoadingThenSuccess_andAddsBreedAndFavourite() = runTest {
        val favouriteRepository = FakeFavouriteImageRepository()
        val breedRepository = FakeCatBreedRepository()
        val useCase = AddToFavourites(
            favouriteRepository = favouriteRepository,
            catBreedRepository = breedRepository
        )
        val image = testImage(id = "img-1")

        useCase(image).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(Unit, success())

            awaitComplete()
        }

        assertEquals(listOf(image.breed), breedRepository.insertedBreeds)
        assertEquals(1, favouriteRepository.addedImages.size)

        val addedFavourite = favouriteRepository.addedImages.single()
        assertEquals(image.id, addedFavourite.imageId)
        assertEquals(image.url, addedFavourite.imageUrl)
        assertEquals(image.breed, addedFavourite.breed)
        assertEquals(SyncStatus.PENDING_SYNC, addedFavourite.syncStatus)
    }

    @Test
    fun invoke_emitsLoadingThenFail_whenInsertBreedThrows() = runTest {
        val expected = IllegalStateException("insert failed")
        val favouriteRepository = FakeFavouriteImageRepository()
        val breedRepository = FakeCatBreedRepository().apply {
            throwOnInsert = expected
        }
        val useCase = AddToFavourites(
            favouriteRepository = favouriteRepository,
            catBreedRepository = breedRepository
        )

        useCase(testImage(id = "img-2")).test {
            assertEquals(Async.Loading, awaitItem())

            val fail = awaitItem()
            assertTrue(fail is Async.Fail)
            assertEquals(expected, fail.error)

            awaitComplete()
        }

        assertTrue(favouriteRepository.addedImages.isEmpty())
    }

    private class FakeCatBreedRepository : CatBreedRepository {
        var throwOnInsert: Throwable? = null
        val insertedBreeds = mutableListOf<CatBreed>()

        override suspend fun getBreeds(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): List<CatBreed> = emptyList()

        override suspend fun getBreed(id: String): CatBreed? = null

        override suspend fun insert(breed: CatBreed) {
            throwOnInsert?.let { throw it }
            insertedBreeds += breed
        }

        override suspend fun delete(breed: CatBreed) = Unit
    }

    private class FakeFavouriteImageRepository : FavouriteImageRepository {
        val addedImages = mutableListOf<FavouriteImage>()

        override fun getFavouriteImages(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Flow<List<FavouriteImage>> = flowOf(emptyList())

        override suspend fun addFavouriteImage(image: FavouriteImage) {
            addedImages += image
        }

        override suspend fun removeFavouriteImage(image: FavouriteImage) = Unit

        override suspend fun synchronize() = Unit
    }

    private fun testImage(id: String) = CatImage(
        id = id,
        url = "https://cdn2.thecatapi.com/images/$id.jpg",
        breed = testBreed(id = "abys")
    )

    private fun testBreed(id: String) = CatBreed(
        id = id,
        name = "Abyssinian",
        altName = "Aby",
        imageId = id,
        origin = "Egypt",
        description = "Active and social.",
        lifeSpan = "14 - 16",
        weight = "8 - 12",
        temperaments = listOf("Active", "Curious", "Gentle"),
        traits = CatTraits(
            adaptability = 5,
            affectionLevel = 5,
            childFriendly = 4,
            dogFriendly = 4,
            energyLevel = 5,
            grooming = 2,
            healthIssues = 2,
            intelligence = 5,
            sheddingLevel = 3,
            socialNeeds = 4,
            strangerFriendly = 4,
            vocalisation = 3,
        ),
        badges = CatBadges(
            isIndoor = true,
            isHypoallergenic = false,
            isHairless = false,
            hasShortLegs = false,
            isLap = true,
        ),
    )
}