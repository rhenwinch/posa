package io.posa.data.repository

import app.cash.turbine.test
import io.posa.core.common.enum.SortOrder
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.datasource.FavouriteImageDataSource
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class FavouriteImageRepositoryImplTest {

    @Test
    fun getFavouriteImages_returnsLocalDatasourceFlow() = runTest {
        val first = testFavouriteImage(id = 1L, syncStatus = SyncStatus.SYNCED)
        val second = testFavouriteImage(id = 2L, syncStatus = SyncStatus.SYNCED)
        val local = FakeFavouriteImageDataSource().apply {
            favouritesFlow = flowOf(listOf(first, second))
        }
        val remote = FakeFavouriteImageDataSource()
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.getFavouriteImages(page = 2, limit = 5, sortOrder = SortOrder.ASC).test {
            assertEquals(listOf(first, second), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, local.getFavouritesCallCount)
        assertEquals(2, local.requestedPage)
        assertEquals(5, local.requestedLimit)
        assertEquals(SortOrder.ASC, local.requestedSortOrder)
        assertEquals(0, remote.getFavouritesCallCount)
    }

    @Test
    fun addFavouriteImage_whenRemoteSucceeds_marksImageAsSyncedLocally() = runTest {
        val image = testFavouriteImage(id = 10L, syncStatus = SyncStatus.PENDING_SYNC)
        val local = FakeFavouriteImageDataSource()
        val remote = FakeFavouriteImageDataSource()
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.addFavouriteImage(image)

        assertEquals(listOf(image, image.copy(syncStatus = SyncStatus.SYNCED)), local.addedFavourites)
        assertEquals(listOf(image), remote.addedFavourites)
        assertEquals(1, remote.addAttempts)
    }

    @Test
    fun addFavouriteImage_whenRemoteFails_keepsOnlyLocalPendingState() = runTest {
        val image = testFavouriteImage(id = 11L, syncStatus = SyncStatus.PENDING_SYNC)
        val local = FakeFavouriteImageDataSource()
        val remote = FakeFavouriteImageDataSource().apply {
            throwOnAdd = true
        }
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.addFavouriteImage(image)

        assertEquals(listOf(image), local.addedFavourites)
        assertTrue(remote.addedFavourites.isEmpty())
        assertEquals(1, remote.addAttempts)
    }

    @Test
    fun removeFavouriteImage_whenRemoteSucceeds_removesFromBothSources() = runTest {
        val image = testFavouriteImage(id = 20L, syncStatus = SyncStatus.SYNCED)
        val local = FakeFavouriteImageDataSource()
        val remote = FakeFavouriteImageDataSource()
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.removeFavouriteImage(image)

        assertEquals(listOf(image.id), local.removedIds)
        assertEquals(listOf(image.id), remote.removedIds)
        assertTrue(local.addedFavourites.isEmpty())
    }

    @Test
    fun removeFavouriteImage_whenRemoteFails_marksPendingDeleteLocally() = runTest {
        val image = testFavouriteImage(id = 21L, syncStatus = SyncStatus.SYNCED)
        val local = FakeFavouriteImageDataSource()
        val remote = FakeFavouriteImageDataSource().apply {
            throwOnRemove = true
        }
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.removeFavouriteImage(image)

        assertEquals(listOf(image.id), local.removedIds)
        assertEquals(1, remote.removeAttempts)
        assertEquals(listOf(image.copy(syncStatus = SyncStatus.PENDING_DELETE)), local.addedFavourites)
    }

    @Test
    fun synchronize_processesPendingSyncAndPendingDelete_andSkipsSynced() = runTest {
        val pendingSync = testFavouriteImage(id = 30L, syncStatus = SyncStatus.PENDING_SYNC)
        val pendingDelete = testFavouriteImage(id = 31L, syncStatus = SyncStatus.PENDING_DELETE)
        val synced = testFavouriteImage(id = 32L, syncStatus = SyncStatus.SYNCED)
        val local = FakeFavouriteImageDataSource().apply {
            pendingSyncFavourites = listOf(pendingSync, pendingDelete, synced)
        }
        val remote = FakeFavouriteImageDataSource()
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.synchronize()

        assertEquals(1, local.getPendingSyncCallCount)
        assertEquals(listOf(pendingSync), remote.addedFavourites)
        assertEquals(listOf(pendingDelete.id), remote.removedIds)
        assertEquals(listOf(pendingSync.copy(syncStatus = SyncStatus.SYNCED)), local.addedFavourites)
        assertEquals(listOf(pendingDelete.id), local.removedIds)
    }

    @Test
    fun synchronize_whenPendingSyncRemoteCallFails_doesNotMarkSyncedLocally() = runTest {
        val pendingSync = testFavouriteImage(id = 40L, syncStatus = SyncStatus.PENDING_SYNC)
        val local = FakeFavouriteImageDataSource().apply {
            pendingSyncFavourites = listOf(pendingSync)
        }
        val remote = FakeFavouriteImageDataSource().apply {
            throwOnAdd = true
        }
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.synchronize()

        assertEquals(1, remote.addAttempts)
        assertTrue(local.addedFavourites.isEmpty())
        assertTrue(local.removedIds.isEmpty())
    }

    @Test
    fun synchronize_whenPendingDeleteRemoteCallFails_doesNotRemoveLocally() = runTest {
        val pendingDelete = testFavouriteImage(id = 41L, syncStatus = SyncStatus.PENDING_DELETE)
        val local = FakeFavouriteImageDataSource().apply {
            pendingSyncFavourites = listOf(pendingDelete)
        }
        val remote = FakeFavouriteImageDataSource().apply {
            throwOnRemove = true
        }
        val repository = FavouriteImageRepositoryImpl(remote = remote, local = local)

        repository.synchronize()

        assertEquals(1, remote.removeAttempts)
        assertTrue(local.removedIds.isEmpty())
        assertTrue(local.addedFavourites.isEmpty())
    }

    private class FakeFavouriteImageDataSource : FavouriteImageDataSource {
        var favouritesFlow: Flow<List<FavouriteImage>> = flowOf(emptyList())
        var pendingSyncFavourites: List<FavouriteImage> = emptyList()

        var getFavouritesCallCount: Int = 0
        var getPendingSyncCallCount: Int = 0
        var requestedPage: Int? = null
        var requestedLimit: Int? = null
        var requestedSortOrder: SortOrder? = null

        var throwOnAdd: Boolean = false
        var throwOnRemove: Boolean = false
        var addAttempts: Int = 0
        var removeAttempts: Int = 0

        val addedFavourites = mutableListOf<FavouriteImage>()
        val removedIds = mutableListOf<Long>()

        override fun getFavourites(
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

        override suspend fun addFavourite(data: FavouriteImage): Long {
            addAttempts += 1
            if (throwOnAdd) {
                throw IllegalStateException("add failed")
            }
            addedFavourites += data
            return data.id
        }

        override suspend fun getPendingSyncFavourites(): List<FavouriteImage> {
            getPendingSyncCallCount += 1
            return pendingSyncFavourites
        }

        override suspend fun removeFavourite(id: Long) {
            removeAttempts += 1
            if (throwOnRemove) {
                throw IllegalStateException("remove failed")
            }
            removedIds += id
        }
    }

    private fun testFavouriteImage(
        id: Long,
        syncStatus: SyncStatus,
    ) = FavouriteImage(
        id = id,
        imageId = "img-$id",
        imageUrl = "https://cdn2.thecatapi.com/images/img-$id.jpg",
        createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L + id),
        breed = testBreed(id = "abys"),
        syncStatus = syncStatus,
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