package io.posa.data.datasource.favourite

import app.cash.turbine.test
import io.posa.core.common.enum.SortOrder
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.core.database.entity.favourite.FavouriteImageWithBreed
import io.posa.core.common.enum.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalFavouriteImageDataSourceTest {

    @Test
    fun getFavourites_usesDescendingDaoFlow_andMapsToDomain() = runTest {
        val dao = FakeFavouriteImageDao()
        val source = LocalFavouriteImageDataSource(favouritesDao = dao)
        val page = 1
        val limit = 2
        val favouriteItems = listOf(
            favouriteWithBreed(id = 100L, imageId = "img-100", createdAt = 1_700_000_100_000L),
            favouriteWithBreed(id = 101L, imageId = "img-101", createdAt = 1_700_000_101_000L),
        )
        dao.descFlow = flowOf(favouriteItems)

        source.getFavourites(
            page = page,
            limit = limit,
            sortOrder = SortOrder.DESC,
        ).test {
            assertEquals(favouriteItems.map { it.toDomain() }, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, dao.descCallCount)
        assertEquals(page, dao.descPage)
        assertEquals(limit, dao.descLimit)
        assertEquals(0, dao.ascCallCount)
    }

    @Test
    fun getFavourites_usesAscendingDaoFlow_whenSortOrderIsNotDescending() = runTest {
        val dao = FakeFavouriteImageDao()
        val source = LocalFavouriteImageDataSource(favouritesDao = dao)
        val favouriteItems = listOf(
            favouriteWithBreed(id = 200L, imageId = "img-200", createdAt = 1_700_000_200_000L),
            favouriteWithBreed(id = 201L, imageId = "img-201", createdAt = 1_700_000_201_000L),
        )
        dao.ascFlow = flowOf(favouriteItems)

        source.getFavourites(
            page = 0,
            limit = 10,
            sortOrder = SortOrder.RANDOM,
        ).test {
            assertEquals(favouriteItems.map { it.toDomain() }, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, dao.ascCallCount)
        assertEquals(0, dao.ascPage)
        assertEquals(10, dao.ascLimit)
        assertEquals(0, dao.descCallCount)
    }

    @Test
    fun addFavourite_convertsDomainAndDelegatesToDao() = runTest {
        val dao = FakeFavouriteImageDao()
        val source = LocalFavouriteImageDataSource(favouritesDao = dao)
        val favourite = favouriteWithBreed(
            id = 300L,
            imageId = "img-300",
            createdAt = 1_700_000_300_000L,
        ).toDomain()

        source.addFavourite(data = favourite)

        assertEquals(FavouriteImageEntity.from(favourite), dao.addedFavourite)
    }

    @Test
    fun getPendingSyncFavourites_mapsDaoPendingItems_toDomain() = runTest {
        val dao = FakeFavouriteImageDao()
        val source = LocalFavouriteImageDataSource(favouritesDao = dao)
        val pendingItems = listOf(
            favouriteWithBreed(
                id = 301L,
                imageId = "img-301",
                createdAt = 1_700_000_301_000L,
            ),
            favouriteWithBreed(
                id = 302L,
                imageId = "img-302",
                createdAt = 1_700_000_302_000L,
            ),
        )
        dao.pendingSyncItems = pendingItems

        val result = source.getPendingSyncFavourites()

        assertEquals(pendingItems.map { it.toDomain() }, result)
        assertEquals(1, dao.pendingSyncCallCount)
    }

    @Test
    fun removeFavourite_delegatesToDaoById() = runTest {
        val dao = FakeFavouriteImageDao()
        val source = LocalFavouriteImageDataSource(favouritesDao = dao)

        source.removeFavourite(id = 999L)

        assertEquals(999L, dao.removedId)
    }

    private class FakeFavouriteImageDao : FavouriteImageDao {
        var descFlow: Flow<List<FavouriteImageWithBreed>> = flowOf(emptyList())
        var ascFlow: Flow<List<FavouriteImageWithBreed>> = flowOf(emptyList())

        var descCallCount: Int = 0
        var ascCallCount: Int = 0

        var descPage: Int? = null
        var descLimit: Int? = null

        var ascPage: Int? = null
        var ascLimit: Int? = null

        var addedFavourite: FavouriteImageEntity? = null
        var removedId: Long? = null
        var pendingSyncItems: List<FavouriteImageWithBreed> = emptyList()
        var pendingSyncCallCount: Int = 0

        override fun getAllDescAsFlow(
            page: Int,
            limit: Int,
        ): Flow<List<FavouriteImageWithBreed>> {
            descCallCount += 1
            descPage = page
            descLimit = limit
            return descFlow
        }

        override fun getAllAscAsFlow(
            page: Int,
            limit: Int,
        ): Flow<List<FavouriteImageWithBreed>> {
            ascCallCount += 1
            ascPage = page
            ascLimit = limit
            return ascFlow
        }

        override suspend fun isFavourite(imageId: String): Boolean =
            error("Not needed for this test")

        override suspend fun getAllPendingSync(): List<FavouriteImageWithBreed> {
            pendingSyncCallCount += 1
            return pendingSyncItems
        }

        override suspend fun add(favouriteImage: FavouriteImageEntity) {
            addedFavourite = favouriteImage
        }

        override suspend fun remove(id: Long) {
            removedId = id
        }

        override suspend fun remove(favouriteImage: FavouriteImageEntity) {
            error("Not needed for this test")
        }
    }

    private fun favouriteWithBreed(
        id: Long,
        imageId: String,
        createdAt: Long,
        breedId: String = "abys",
        breedName: String = "Abyssinian",
    ) = FavouriteImageWithBreed(
        favouriteImage = FavouriteImageEntity(
            id = id,
            breedId = breedId,
            imageId = imageId,
            imageUrl = "https://cdn2.thecatapi.com/images/$imageId.jpg",
            createdAt = createdAt,
            syncStatus = SyncStatus.SYNCED,
        ),
        breed = CatBreedEntityWithTraitsAndBadges(
            breed = CatBreedEntity(
                id = breedId,
                name = breedName,
                altName = "Aby",
                imageUrl = "https://example.com/$breedId.jpg",
                origin = "Egypt",
                description = "$breedName is playful and social.",
                lifeSpan = "14 - 16",
                weight = "8 - 12",
                temperaments = "Active, Curious, Gentle",
            ),
            traits = CatTraitsEntity(
                breedId = breedId,
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
            badges = CatBadgesEntity(
                breedId = breedId,
                isIndoor = true,
                isHypoallergenic = false,
                isHairless = false,
                hasShortLegs = false,
                isLap = true,
            ),
        ),
    )
}