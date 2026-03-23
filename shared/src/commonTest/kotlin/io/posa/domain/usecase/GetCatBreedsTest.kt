package io.posa.domain.usecase

import app.cash.turbine.test
import io.posa.core.common.Async
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.repository.CatBreedRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCatBreedsTest {

    @Test
    fun invoke_emitsLoadingThenSuccess_andRequestsRepositoryWithDefaultLimit() = runTest {
        val repository = FakeCatBreedRepository().apply {
            responses += listOf(
                testBreed("abys"),
                testBreed("beng"),
            )
        }
        val useCase = GetCatBreeds(catBreedRepository = repository)

        useCase(page = 3, sortOrder = SortOrder.DESC).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(listOf(testBreed("abys"), testBreed("beng")), success())

            awaitComplete()
        }

        assertEquals(listOf(3), repository.requestedPages)
        assertEquals(listOf(10), repository.requestedLimits)
        assertEquals(listOf(SortOrder.DESC), repository.requestedSortOrders)
    }

    @Test
    fun invoke_filtersAlreadyShownBreeds_acrossCalls() = runTest {
        val repository = FakeCatBreedRepository().apply {
            responses += listOf(testBreed("abys"), testBreed("beng"))
            responses += listOf(testBreed("beng"), testBreed("siam"))
        }
        val useCase = GetCatBreeds(catBreedRepository = repository)

        useCase(page = 0, sortOrder = SortOrder.RANDOM).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(listOf(testBreed("abys"), testBreed("beng")), success())

            awaitComplete()
        }

        useCase(page = 1, sortOrder = SortOrder.RANDOM).test {
            assertEquals(Async.Loading, awaitItem())

            val success = awaitItem()
            assertTrue(success is Async.Success)
            assertEquals(listOf(testBreed("siam")), success())

            awaitComplete()
        }
    }

    @Test
    fun invoke_emitsLoadingThenFail_whenRepositoryThrows() = runTest {
        val expected = IllegalStateException("network failed")
        val repository = FakeCatBreedRepository().apply {
            throwOnGetBreeds = expected
        }
        val useCase = GetCatBreeds(catBreedRepository = repository)

        useCase(page = 1, sortOrder = SortOrder.ASC).test {
            assertEquals(Async.Loading, awaitItem())

            val fail = awaitItem()
            assertTrue(fail is Async.Fail)
            assertEquals(expected, fail.error)

            awaitComplete()
        }
    }

    private class FakeCatBreedRepository : CatBreedRepository {
        val responses = mutableListOf<List<CatBreed>>()
        var throwOnGetBreeds: Throwable? = null

        val requestedPages = mutableListOf<Int>()
        val requestedLimits = mutableListOf<Int>()
        val requestedSortOrders = mutableListOf<SortOrder>()

        override suspend fun getBreeds(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): List<CatBreed> {
            requestedPages += page
            requestedLimits += limit
            requestedSortOrders += sortOrder

            throwOnGetBreeds?.let { throw it }
            return responses.removeFirstOrNull() ?: emptyList()
        }

        override suspend fun getBreed(id: String): CatBreed? = null

        override suspend fun insert(breed: CatBreed) = Unit

        override suspend fun delete(breed: CatBreed) = Unit
    }

    private fun testBreed(id: String) = CatBreed(
        id = id,
        name = "Breed-$id",
        altName = null,
        imageId = id,
        origin = "Egypt",
        description = "Friendly and active.",
        lifeSpan = "12 - 16",
        weight = "8 - 12",
        temperaments = listOf("Active", "Curious"),
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
            isLap = false,
        ),
    )
}