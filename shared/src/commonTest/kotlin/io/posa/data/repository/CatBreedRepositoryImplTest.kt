package io.posa.data.repository

import io.posa.core.common.enum.SortOrder
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatBreedRepositoryImplTest {

    @Test
    fun getBreeds_delegatesToRemote_andReturnsRemoteResult() = runTest {
        val remoteBreeds = listOf(
            testBreed(id = "abys"),
            testBreed(id = "beng"),
        )
        val local = FakeCatBreedDataSource()
        val remote = FakeCatBreedDataSource().apply {
            getBreedsResult = remoteBreeds
        }
        val repository = CatBreedRepositoryImpl(remote = remote, local = local)

        val result = repository.getBreeds(
            page = 2,
            limit = 20,
            sortOrder = SortOrder.DESC,
        )

        assertEquals(remoteBreeds, result)
        assertEquals(listOf(2), remote.requestedPages)
        assertEquals(listOf(20), remote.requestedLimits)
        assertEquals(listOf(SortOrder.DESC), remote.requestedSortOrders)
        assertTrue(local.requestedPages.isEmpty())
    }

    @Test
    fun getBreed_returnsLocalBreed_whenLocalHasData() = runTest {
        val localBreed = testBreed(id = "abys")
        val local = FakeCatBreedDataSource().apply {
            getBreedResult = localBreed
        }
        val remote = FakeCatBreedDataSource().apply {
            getBreedResult = testBreed(id = "abys")
        }
        val repository = CatBreedRepositoryImpl(remote = remote, local = local)

        val result = repository.getBreed(id = "abys")

        assertEquals(localBreed, result)
        assertTrue(local.requestedBreedIds.isNotEmpty())
        assertTrue(remote.requestedBreedIds.isEmpty())
    }

    @Test
    fun getBreed_fallsBackToRemote_whenLocalIsMissing() = runTest {
        val remoteBreed = testBreed(id = "abys")
        val local = FakeCatBreedDataSource().apply {
            getBreedResult = null
        }
        val remote = FakeCatBreedDataSource().apply {
            getBreedResult = remoteBreed
        }
        val repository = CatBreedRepositoryImpl(remote = remote, local = local)

        val result = repository.getBreed(id = "abys")

        assertEquals(remoteBreed, result)
        assertEquals(listOf("abys"), local.requestedBreedIds)
        assertEquals(listOf("abys"), remote.requestedBreedIds)
    }

    @Test
    fun insert_delegatesToLocalOnly() = runTest {
        val breed = testBreed(id = "beng")
        val local = FakeCatBreedDataSource()
        val remote = FakeCatBreedDataSource()
        val repository = CatBreedRepositoryImpl(remote = remote, local = local)

        repository.insert(breed)

        assertEquals(listOf(breed), local.insertedBreeds)
        assertTrue(remote.insertedBreeds.isEmpty())
    }

    @Test
    fun delete_delegatesToLocalOnly() = runTest {
        val breed = testBreed(id = "siam")
        val local = FakeCatBreedDataSource()
        val remote = FakeCatBreedDataSource()
        val repository = CatBreedRepositoryImpl(remote = remote, local = local)

        repository.delete(breed)

        assertEquals(listOf(breed), local.deletedBreeds)
        assertTrue(remote.deletedBreeds.isEmpty())
    }

    private class FakeCatBreedDataSource : CatBreedDataSource {
        var getBreedsResult: List<CatBreed> = emptyList()
        var getBreedResult: CatBreed? = null
        val requestedPages = mutableListOf<Int>()
        val requestedLimits = mutableListOf<Int>()
        val requestedSortOrders = mutableListOf<SortOrder>()
        val requestedBreedIds = mutableListOf<String>()
        val insertedBreeds = mutableListOf<CatBreed>()
        val deletedBreeds = mutableListOf<CatBreed>()
        val deletedIds = mutableListOf<String>()

        override suspend fun getBreeds(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): List<CatBreed> {
            requestedPages += page
            requestedLimits += limit
            requestedSortOrders += sortOrder
            return getBreedsResult
        }

        override suspend fun insert(breed: CatBreed) {
            insertedBreeds += breed
        }

        override suspend fun delete(id: String) {
            deletedIds += id
        }

        override suspend fun delete(breed: CatBreed) {
            deletedBreeds += breed
        }

        override suspend fun getBreed(id: String): CatBreed? {
            requestedBreedIds += id
            return getBreedResult
        }
    }

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
            vocalisation = 3
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