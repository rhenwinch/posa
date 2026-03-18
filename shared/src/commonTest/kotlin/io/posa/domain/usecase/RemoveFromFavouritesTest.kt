package io.posa.domain.usecase

import app.cash.turbine.test
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.repository.FavouriteImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class RemoveFromFavouritesTest {

    @Test
    fun invoke_emitsLoadingThenSuccess_andRemovesFavourite() = runTest {
        val repository = FakeFavouriteImageRepository()
        val useCase = RemoveFromFavourites(repository = repository)
        val favourite = testFavourite(id = 77L)

        useCase(favourite).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(Unit, success())

            awaitComplete()
        }

        assertEquals(listOf(favourite), repository.removedImages)
    }

    @Test
    fun invoke_emitsLoadingThenFail_whenRepositoryThrows() = runTest {
        val expected = IllegalStateException("remove failed")
        val repository = FakeFavouriteImageRepository().apply {
            throwOnRemove = expected
        }
        val useCase = RemoveFromFavourites(repository = repository)

        useCase(testFavourite(id = 88L)).test {
            assertEquals(Async.Loading, awaitItem())

            val fail = awaitItem()
            assertTrue(fail is Async.Fail)
            assertEquals(expected, fail.error)

            awaitComplete()
        }
    }

    private class FakeFavouriteImageRepository : FavouriteImageRepository {
        var throwOnRemove: Throwable? = null
        val removedImages = mutableListOf<FavouriteImage>()

        override fun getFavouriteImages(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Flow<List<FavouriteImage>> = flowOf(emptyList())

        override suspend fun addFavouriteImage(image: FavouriteImage) = Unit

        override suspend fun removeFavouriteImage(image: FavouriteImage) {
            throwOnRemove?.let { throw it }
            removedImages += image
        }

        override suspend fun synchronize() = Unit
    }

    private fun testFavourite(id: Long) = FavouriteImage(
        id = id,
        imageId = "img-$id",
        imageUrl = "https://cdn2.thecatapi.com/images/img-$id.jpg",
        createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L + id),
        breed = testBreed(id = "abys"),
        syncStatus = SyncStatus.SYNCED,
    )

    private fun testBreed(id: String) = CatBreed(
        id = id,
        name = "Abyssinian",
        altName = "Aby",
        imageUrl = "https://example.com/$id.jpg",
        origin = "Egypt",
        description = "Playful and social.",
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