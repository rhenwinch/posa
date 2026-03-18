package io.posa.data.datasource.favourite

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import app.cash.turbine.test
import io.posa.core.common.enum.SortOrder
import io.posa.core.datastore.PosaDataStore
import io.pusa.network.TheCatApiService
import io.pusa.network.dto.CatBreedDto
import io.pusa.network.dto.CatFavouriteDto
import io.pusa.network.dto.CatImageDto
import io.pusa.network.dto.CommonResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RemoteFavouriteImagesDataSourceTest {

    @Test
    fun getFavourites_emitsEmptyList_whenUserIdIsMissing() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = null)
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)

        source.getFavourites(
            page = 0,
            limit = 10,
            sortOrder = SortOrder.DESC,
        ).test {
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(0, api.getFavouritesCallCount)
    }

    @Test
    fun getFavourites_usesDescOrder_andMapsDtos_whenUserIdExists() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = "user-1")
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)
        val favourites = listOf(
            favouriteDto(id = 1L, imageId = "img-1", createdAt = 1_700_000_001_000L),
            favouriteDto(id = 2L, imageId = "img-2", createdAt = 1_700_000_002_000L),
        )
        api.favouritesFlow = flowOf(favourites)

        source.getFavourites(
            page = 2,
            limit = 5,
            sortOrder = SortOrder.DESC,
        ).test {
            assertEquals(favourites.map { it.toDomain() }, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, api.getFavouritesCallCount)
        assertEquals("user-1", api.lastGetFavouritesUserId)
        assertEquals(2, api.lastGetFavouritesPage)
        assertEquals(5, api.lastGetFavouritesLimit)
        assertEquals("DESC", api.lastGetFavouritesOrder)
    }

    @Test
    fun getFavourites_usesAscOrder_whenSortOrderIsNotDescending() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = "user-2")
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)
        api.favouritesFlow = flowOf(emptyList())

        source.getFavourites(
            page = 0,
            limit = 20,
            sortOrder = SortOrder.ASC,
        ).test {
            assertEquals(emptyList(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("ASC", api.lastGetFavouritesOrder)
    }

    @Test
    fun addFavourite_callsApiWithImageIdAndUserId() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = "user-3")
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)
        val favourite = favouriteDto(id = 5L, imageId = "img-5", createdAt = 1_700_000_005_000L).toDomain()

        source.addFavourite(data = favourite)

        assertEquals("img-5", api.lastAddFavouriteImageId)
        assertEquals("user-3", api.lastAddFavouriteUserId)
    }

    @Test
    fun addFavourite_throws_whenUserIdIsMissing() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = null)
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)
        val favourite = favouriteDto(id = 6L, imageId = "img-6", createdAt = 1_700_000_006_000L).toDomain()

        val error = assertFailsWith<IllegalArgumentException> {
            source.addFavourite(data = favourite)
        }

        assertEquals(
            "User ID must be set in DataStore to add a favourite image.",
            error.message
        )
    }

    @Test
    fun removeFavourite_callsApiWithFavouriteId_whenUserIdExists() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = "user-4")
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)

        source.removeFavourite(id = 42L)

        assertEquals(42L, api.lastRemoveFavouriteId)
    }

    @Test
    fun removeFavourite_throws_whenUserIdIsMissing() = runTest {
        val api = FakeTheCatApiService()
        val dataStore = FakePreferencesDataStore(userId = null)
        val source = RemoteFavouriteImagesDataSource(api = api, dataStore = dataStore)

        val error = assertFailsWith<IllegalArgumentException> {
            source.removeFavourite(id = 99L)
        }

        assertEquals(
            "User ID must be set in DataStore to remove a favourite image.",
            error.message
        )
    }

    private class FakePreferencesDataStore(userId: String?) : DataStore<Preferences> {
        private val state = MutableStateFlow(initialPreferences(userId))

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }

        private companion object {
            fun initialPreferences(userId: String?): Preferences {
                return if (userId == null) {
                    emptyPreferences()
                } else {
                    preferencesOf(PosaDataStore.PREF_USER_ID to userId)
                }
            }
        }
    }

    private class FakeTheCatApiService : TheCatApiService {
        var favouritesFlow: Flow<List<CatFavouriteDto>> = flowOf(emptyList())

        var getFavouritesCallCount: Int = 0
        var lastGetFavouritesUserId: String? = null
        var lastGetFavouritesPage: Int? = null
        var lastGetFavouritesLimit: Int? = null
        var lastGetFavouritesOrder: String? = null

        var lastAddFavouriteImageId: String? = null
        var lastAddFavouriteUserId: String? = null
        var lastRemoveFavouriteId: Long? = null

        override suspend fun getCatImages(
            page: Int,
            hasBreeds: Boolean,
            size: String,
            format: String,
            order: String,
            limit: Int,
        ): List<CatImageDto> = error("Not needed for this test")

        override suspend fun getCatImage(id: String): CatImageDto =
            error("Not needed for this test")

        override suspend fun getBreeds(
            page: Int,
            order: String,
            limit: Int,
        ): List<CatBreedDto> = error("Not needed for this test")

        override suspend fun searchBreeds(
            query: String,
            attachImage: Boolean,
        ): List<CatBreedDto> = error("Not needed for this test")

        override suspend fun getBreed(id: String): CatBreedDto =
            error("Not needed for this test")

        override fun getFavourites(
            userId: String,
            page: Int,
            limit: Int,
            order: String,
        ): Flow<List<CatFavouriteDto>> {
            getFavouritesCallCount += 1
            lastGetFavouritesUserId = userId
            lastGetFavouritesPage = page
            lastGetFavouritesLimit = limit
            lastGetFavouritesOrder = order
            return favouritesFlow
        }

        override suspend fun addFavourite(
            imageId: String,
            userId: String,
        ): CommonResponseDto {
            lastAddFavouriteImageId = imageId
            lastAddFavouriteUserId = userId
            return CommonResponseDto(id = 1L, message = "OK")
        }

        override suspend fun removeFavourite(id: Long): CommonResponseDto {
            lastRemoveFavouriteId = id
            return CommonResponseDto(message = "OK")
        }
    }

    private fun favouriteDto(
        id: Long,
        imageId: String,
        createdAt: Long,
        breedId: String = "abys",
        breedName: String = "Abyssinian",
    ) = CatFavouriteDto(
        id = id,
        image = CatImageDto(
            id = imageId,
            url = "https://cdn2.thecatapi.com/images/$imageId.jpg",
            width = 1200,
            height = 800,
            breeds = listOf(
                breedDto(
                    id = breedId,
                    name = breedName,
                    referenceImageId = imageId,
                )
            ),
        ),
        createdAt = kotlin.time.Instant.fromEpochMilliseconds(createdAt),
        imageId = imageId,
        subId = "sub-123",
        userId = "api-user-123",
    )

    private fun breedDto(
        id: String,
        name: String,
        referenceImageId: String,
    ) = CatBreedDto(
        id = id,
        name = name,
        weightDto = CatBreedDto.WeightDto(
            imperial = "8 - 12",
            metric = "3 - 5",
        ),
        temperament = "Active, Curious, Gentle",
        origin = "Egypt",
        description = "$name is playful and social.",
        grooming = 2,
        intelligence = 5,
        vocalisation = 3,
        adaptability = 5,
        countryCodes = "US",
        countryCode = "US",
        lifeSpan = "14 - 16",
        affectionLevel = 5,
        altNames = "Aby",
        childFriendly = 4,
        dogFriendly = 4,
        energyLevel = 5,
        healthIssues = 2,
        referenceImageId = referenceImageId,
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
}