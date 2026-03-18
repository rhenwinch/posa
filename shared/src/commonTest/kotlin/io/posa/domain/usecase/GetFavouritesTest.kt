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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetFavouritesTest {

    @Test
    fun invoke_emitsLoadingThenSuccess_andUsesRepositoryDefaultLimit() = runTest {
        val favourites = listOf(
            testFavourite(id = 1L),
            testFavourite(id = 2L),
        )
        val repository = FakeFavouriteImageRepository().apply {
            favouritesFlow = flowOf(favourites)
        }
        val useCase = GetFavourites(repository = repository)

        useCase(page = 4, sortOrder = SortOrder.ASC).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(favourites, success())

            awaitComplete()
        }

        assertEquals(1, repository.getFavouritesCallCount)
        assertEquals(4, repository.requestedPage)
        assertEquals(10, repository.requestedLimit)
        assertEquals(SortOrder.ASC, repository.requestedSortOrder)
    }

    @Test
    fun invoke_emitsLoadingThenFail_whenRepositoryFlowThrows() = runTest {
        val expected = IllegalStateException("load failed")
        val repository = FakeFavouriteImageRepository().apply {
            favouritesFlow = flow { throw expected }
        }
        val useCase = GetFavourites(repository = repository)

        useCase(page = 0, sortOrder = SortOrder.DESC).test {
            assertEquals(Async.Loading, awaitItem())

            val fail = awaitItem()
            assertTrue(fail is Async.Fail)
            assertEquals(expected, fail.error)

            awaitComplete()
        }
    }

    private class FakeFavouriteImageRepository : FavouriteImageRepository {
        var favouritesFlow: Flow<List<FavouriteImage>> = flowOf(emptyList())
        var getFavouritesCallCount: Int = 0
        var requestedPage: Int? = null
        var requestedLimit: Int? = null
        var requestedSortOrder: SortOrder? = null

        override fun getFavouriteImages(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Flow<List<FavouriteImage>> {
            getFavouritesCallCount += 1
            requestedPage = page
            requestedLimit = limit
            requestedSortOrder = sortOrder
            return favouritesFlow
        }

        override suspend fun addFavouriteImage(image: FavouriteImage) = Unit

        override suspend fun removeFavouriteImage(image: FavouriteImage) = Unit

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