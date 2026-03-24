package io.posa.core.database.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.posa.core.common.enum.SyncStatus
import io.posa.core.database.PosaDatabase
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteImageDaoInstrumentedTest {

    private lateinit var database: PosaDatabase
    private lateinit var catBreedDao: CatBreedDao
    private lateinit var favouriteImageDao: FavouriteImageDao

    @Before
    fun setUp() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            PosaDatabase::class.java
        ).build()

        catBreedDao = database.catBreedDao
        favouriteImageDao = database.favouriteImageDao

        // Favourites require an existing breed because of foreign key constraints.
        insertBreedGraph(breedId = DEFAULT_BREED_ID, breedName = "Abyssinian")
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllDescAndAscAsFlow_sortsByCreatedAt_andExcludesPendingDelete() = runTest {
        favouriteImageDao.add(
            favouriteImage = favouriteEntity(
                id = 1L,
                imageId = "img-1",
                createdAt = 1_700_000_100_000L
            )
        )
        favouriteImageDao.add(
            favouriteImage = favouriteEntity(
                id = 2L,
                imageId = "img-2",
                createdAt = 1_700_000_300_000L
            )
        )
        favouriteImageDao.add(
            favouriteImage = favouriteEntity(
                id = 3L,
                imageId = "img-3",
                createdAt = 1_700_000_200_000L
            )
        )
        favouriteImageDao.add(
            favouriteImage = favouriteEntity(
                id = 4L,
                imageId = "img-4",
                createdAt = 1_700_000_500_000L,
                syncStatus = SyncStatus.PENDING_DELETE,
            )
        )

        val descIds = favouriteImageDao.getAllDescAsFlow()
            .first()
            .map { it.favouriteImage.id }
        val ascIds = favouriteImageDao.getAllAscAsFlow()
            .first()
            .map { it.favouriteImage.id }

        assertEquals(listOf(2L, 3L, 1L), descIds)
        assertEquals(listOf(1L, 3L, 2L), ascIds)
    }

    @Test
    fun addAndIsFavourite_andReplaceOnConflict_behaveLikeSyncUpdates() = runTest {
        val initial = favouriteEntity(
            id = 10L,
            imageId = "img-10",
            createdAt = 1_700_000_100_000L,
            imageUrl = "https://cdn2.thecatapi.com/images/img-10.jpg",
            syncStatus = SyncStatus.PENDING_SYNC
        )
        val updated = favouriteEntity(
            id = 10L,
            imageId = "img-10-v2",
            createdAt = 1_700_000_400_000L,
            imageUrl = "https://cdn2.thecatapi.com/images/img-10-v2.jpg",
            syncStatus = SyncStatus.SYNCED
        )

        favouriteImageDao.add(favouriteImage = initial)
        assertTrue(favouriteImageDao.isFavourite(breedId = "img-10"))

        favouriteImageDao.add(favouriteImage = updated)

        val stored = favouriteImageDao.getAllDescAsFlow().first()

        assertFalse(favouriteImageDao.isFavourite(breedId = "img-10"))
        assertTrue(favouriteImageDao.isFavourite(breedId = "img-10-v2"))
        assertEquals(1, stored.size)
        assertEquals("img-10-v2", stored.single().favouriteImage.imageId)
        assertEquals("https://cdn2.thecatapi.com/images/img-10-v2.jpg", stored.single().favouriteImage.imageUrl)
        assertEquals(SyncStatus.SYNCED, stored.single().favouriteImage.syncStatus)
    }

    @Test
    fun getAllPendingSync_returnsOnlyPendingSyncItems_inDescendingCreatedAtOrder() = runTest {
        val pendingOld = favouriteEntity(
            id = 30L,
            imageId = "img-30",
            createdAt = 1_700_000_100_000L,
            syncStatus = SyncStatus.PENDING_SYNC,
        )
        val synced = favouriteEntity(
            id = 31L,
            imageId = "img-31",
            createdAt = 1_700_000_200_000L,
            syncStatus = SyncStatus.SYNCED,
        )
        val pendingNew = favouriteEntity(
            id = 32L,
            imageId = "img-32",
            createdAt = 1_700_000_300_000L,
            syncStatus = SyncStatus.PENDING_SYNC,
        )
        val pendingDelete = favouriteEntity(
            id = 33L,
            imageId = "img-33",
            createdAt = 1_700_000_400_000L,
            syncStatus = SyncStatus.PENDING_DELETE,
        )

        favouriteImageDao.add(pendingOld)
        favouriteImageDao.add(synced)
        favouriteImageDao.add(pendingNew)
        favouriteImageDao.add(pendingDelete)

        val pending = favouriteImageDao.getAllPendingSync()

        assertEquals(listOf(32L, 30L), pending.map { it.favouriteImage.id })
        assertTrue(pending.all { it.favouriteImage.syncStatus == SyncStatus.PENDING_SYNC })
    }

    @Test
    fun removeByImageIdByEntity_coverTypicalUnfavouriteFlows() = runTest {
        val first = favouriteEntity(id = 20L, imageId = "img-20", createdAt = 1_700_000_100_000L)
        val second = favouriteEntity(id = 21L, imageId = "img-21", createdAt = 1_700_000_200_000L)
        val third = favouriteEntity(id = 22L, imageId = "img-22", createdAt = 1_700_000_300_000L)

        favouriteImageDao.add(first)
        favouriteImageDao.add(second)
        favouriteImageDao.add(third)

        favouriteImageDao.remove(id = first.id)
        assertFalse(favouriteImageDao.isFavourite(breedId = first.imageId))

        favouriteImageDao.remove(favouriteImage = second)
        assertFalse(favouriteImageDao.isFavourite(breedId = second.imageId))
        assertTrue(favouriteImageDao.isFavourite(breedId = third.imageId))
    }

    private suspend fun insertBreedGraph(
        breedId: String,
        breedName: String,
    ) {
        val breedEntity = CatBreedEntity(
            id = breedId,
            name = breedName,
            altName = null,
            imageId = breedId,
            origin = "Egypt",
            description = "$breedName is playful and social.",
            lifeSpan = "12 - 16",
            weight = "8 - 12",
            temperaments = "Active, Curious, Gentle",
        )
        val traitsEntity = CatTraitsEntity(
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
            vocalisation = 3
        )
        val badgesEntity = CatBadgesEntity(
            breedId = breedId,
            isIndoor = true,
            isHypoallergenic = false,
            isHairless = false,
            hasShortLegs = false,
            isLap = true,
        )

        catBreedDao.insert(breedEntity)
        catBreedDao.insert(traitsEntity)
        catBreedDao.insert(badgesEntity)
    }

    private fun favouriteEntity(
        id: Long,
        imageId: String,
        createdAt: Long,
        imageUrl: String = "https://cdn2.thecatapi.com/images/$imageId.jpg",
        syncStatus: SyncStatus = SyncStatus.SYNCED,
    ) = FavouriteImageEntity(
        id = id,
        breedId = DEFAULT_BREED_ID,
        imageId = imageId,
        imageUrl = imageUrl,
        createdAt = createdAt,
        syncStatus = syncStatus
    )

    private companion object {
        const val DEFAULT_BREED_ID = "abys"
    }
}