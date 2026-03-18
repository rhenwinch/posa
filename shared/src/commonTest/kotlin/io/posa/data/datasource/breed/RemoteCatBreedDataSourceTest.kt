package io.posa.data.datasource.breed

import io.posa.core.common.enum.Measurement
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.core.common.enum.SyncStatus
import io.pusa.network.TheCatApiService
import io.pusa.network.dto.CatBreedDto
import io.pusa.network.dto.CatFavouriteDto
import io.pusa.network.dto.CatImageDto
import io.pusa.network.dto.CommonResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RemoteCatBreedDataSourceTest {
    companion object {
        private fun testBreedDto(id: String = "abys") = CatBreedDto(
            id = id,
            name = "Abyssinian",
            weightDto = CatBreedDto.WeightDto(
                imperial = "8 - 12",
                metric = "3 - 5"
            ),
            temperament = "Active, Curious, Gentle",
            origin = "Egypt",
            description = "Active and affectionate.",
            grooming = 2,
            intelligence = 5,
            vocalisation = 3,
            adaptability = 5,
            countryCodes = "US",
            countryCode = "US",
            lifeSpan = "14 - 15",
            affectionLevel = 5,
            altNames = "Aby",
            childFriendly = 4,
            dogFriendly = 4,
            energyLevel = 5,
            healthIssues = 2,
            referenceImageId = "abc123",
            sheddingLevel = 3,
            socialNeeds = 4,
            strangerFriendly = 4,
            isExperimental = false,
            isHairless = false,
            isNatural = true,
            isRare = false,
            rex = false,
            hasSuppressedTail = false,
            hasShortLegs = false,
            isHypoallergenic = false,
            isIndoor = true,
            isLap = true,
        )

        private fun testDomainBreed(id: String = "abys") = CatBreed(
            id = id,
            name = "Abyssinian",
            altName = "Aby",
            imageUrl = "https://example.com/abys.jpg",
            origin = "Egypt",
            description = "Active and affectionate.",
            lifeSpan = "14 - 15",
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

    @Test
    fun getBreed_requestsBreedById_andMapsUsingImperialMeasurement() = runTest {
        val api = FakeTheCatApiService().apply {
            breedResult = testBreedDto()
        }
        val dataSource = RemoteCatBreedDataSource(apiService = api)

        val result = dataSource.getBreed(id = "abys")

        assertEquals("abys", api.requestedBreedId)
        assertEquals(testBreedDto().toDomain(), result)
        assertEquals("8 - 12", result?.weight)
    }

    @Test
    fun insert_throwsUnsupportedOperationException() = runTest {
        val api = FakeTheCatApiService()
        val dataSource = RemoteCatBreedDataSource(apiService = api)

        val error = assertFailsWith<UnsupportedOperationException> {
            dataSource.insert(breed = testDomainBreed())
        }

        assertEquals("Cannot insert breed into remote data source", error.message)
    }

    @Test
    fun deleteById_throwsUnsupportedOperationException() = runTest {
        val api = FakeTheCatApiService()
        val dataSource = RemoteCatBreedDataSource(apiService = api)

        val error = assertFailsWith<UnsupportedOperationException> {
            dataSource.delete(id = "abys")
        }

        assertEquals("Cannot delete breed from remote data source", error.message)
    }

    @Test
    fun deleteByBreed_throwsUnsupportedOperationException() = runTest {
        val api = FakeTheCatApiService()
        val dataSource = RemoteCatBreedDataSource(apiService = api)

        val error = assertFailsWith<UnsupportedOperationException> {
            dataSource.delete(breed = testDomainBreed())
        }

        assertEquals("Cannot delete breed from remote data source", error.message)
    }

    private class FakeTheCatApiService : TheCatApiService {
        var requestedBreedId: String? = null
        var breedResult: CatBreedDto = testBreedDto()

        override suspend fun getCatImages(
            page: Int,
            hasBreeds: Boolean,
            size: String,
            format: String,
            order: String,
            limit: Int
        ): List<CatImageDto> = error("Not needed for this test")

        override suspend fun getCatImage(id: String): CatImageDto = error("Not needed for this test")

        override suspend fun getBreeds(page: Int, order: String, limit: Int): List<CatBreedDto> =
            error("Not needed for this test")

        override suspend fun searchBreeds(query: String, attachImage: Boolean): List<CatBreedDto> =
            error("Not needed for this test")

        override suspend fun getBreed(id: String): CatBreedDto {
            requestedBreedId = id
            return breedResult
        }

        override suspend fun addFavourite(imageId: String, userId: String): CommonResponseDto =
            error("Not needed for this test")

        override suspend fun removeFavourite(id: Long): CommonResponseDto =
            error("Not needed for this test")

        override fun getFavourites(
            userId: String,
            page: Int,
            limit: Int,
            order: String
        ): Flow<List<CatFavouriteDto>> = error("Not needed for this test")
    }
}