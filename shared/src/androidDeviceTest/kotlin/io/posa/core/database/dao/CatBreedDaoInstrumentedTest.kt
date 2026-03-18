package io.posa.core.database.dao

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.posa.core.database.PosaDatabase
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.domain.model.sync.SyncStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatBreedDaoInstrumentedTest {

    private lateinit var database: PosaDatabase
    private lateinit var catBreedDao: CatBreedDao
    private lateinit var favouriteImageDao: FavouriteImageDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        database = Room.inMemoryDatabaseBuilder(
            context,
            PosaDatabase::class.java
        ).build()

        catBreedDao = database.catBreedDao
        favouriteImageDao = database.favouriteImageDao
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetBreed_returnsBreedWithTraitsAndBadges() = runBlocking {
        val breedId = "abys"
        val breedEntity = breedEntity(id = breedId, name = "Abyssinian")
        val traitsEntity = traitsEntity(breedId = breedId)
        val badgesEntity = badgesEntity(breedId = breedId)

        catBreedDao.insert(breed = breedEntity)
        catBreedDao.insert(traits = traitsEntity)
        catBreedDao.insert(badges = badgesEntity)

        val stored = checkNotNull(catBreedDao.getBreed(id = breedId))
        val storedDomain = checkNotNull(stored.toDomain())

        assertEquals(breedEntity.id, stored.breed.id)
        assertEquals(breedEntity.name, stored.breed.name)
        assertEquals(traitsEntity.adaptability, stored.traits.adaptability)
        assertEquals(badgesEntity.isIndoor, stored.badges.isIndoor)
        assertEquals(listOf("Active", "Curious", "Gentle"), storedDomain.temperaments)
    }

    @Test
    fun deleteById_removesBreedAndCascadesToFavourites() = runBlocking {
        val breedId = "abys"
        insertBreedGraph(breedId = breedId, breedName = "Abyssinian")

        val favourite = favouriteEntity(
            id = 100L,
            breedId = breedId,
            imageId = "img-abys-100",
            createdAt = 1_700_000_100_000L
        )
        favouriteImageDao.add(favouriteImage = favourite)

        assertTrue(favouriteImageDao.isFavourite(imageId = favourite.imageId))

        catBreedDao.delete(id = breedId)

        assertNull(catBreedDao.getBreed(id = breedId))
        assertFalse(favouriteImageDao.isFavourite(imageId = favourite.imageId))
    }

    @Test
    fun deleteByEntity_removesBreedAndCascadesToFavourites() = runBlocking {
        val breedId = "beng"
        val breed = insertBreedGraph(breedId = breedId, breedName = "Bengal")

        val favourite = favouriteEntity(
            id = 101L,
            breedId = breedId,
            imageId = "img-beng-101",
            createdAt = 1_700_000_101_000L
        )
        favouriteImageDao.add(favouriteImage = favourite)

        assertTrue(favouriteImageDao.isFavourite(imageId = favourite.imageId))

        catBreedDao.delete(breed = breed)

        assertNull(catBreedDao.getBreed(id = breedId))
        assertFalse(favouriteImageDao.isFavourite(imageId = favourite.imageId))
    }

    @Test
    fun deleteAll_removesAllBreedsAndTheirFavourites() = runBlocking {
        insertBreedGraph(breedId = "abys", breedName = "Abyssinian")
        insertBreedGraph(breedId = "siam", breedName = "Siamese")

        val firstFavourite = favouriteEntity(
            id = 201L,
            breedId = "abys",
            imageId = "img-abys-201",
            createdAt = 1_700_000_201_000L
        )
        val secondFavourite = favouriteEntity(
            id = 202L,
            breedId = "siam",
            imageId = "img-siam-202",
            createdAt = 1_700_000_202_000L
        )

        favouriteImageDao.add(favouriteImage = firstFavourite)
        favouriteImageDao.add(favouriteImage = secondFavourite)

        catBreedDao.deleteAll()

        assertNull(catBreedDao.getBreed(id = "abys"))
        assertNull(catBreedDao.getBreed(id = "siam"))
        assertFalse(favouriteImageDao.isFavourite(imageId = firstFavourite.imageId))
        assertFalse(favouriteImageDao.isFavourite(imageId = secondFavourite.imageId))
    }

    private suspend fun insertBreedGraph(
        breedId: String,
        breedName: String,
    ): CatBreedEntity {
        val breedEntity = breedEntity(id = breedId, name = breedName)
        catBreedDao.insert(breed = breedEntity)
        catBreedDao.insert(traits = traitsEntity(breedId = breedId))
        catBreedDao.insert(badges = badgesEntity(breedId = breedId))
        return breedEntity
    }

    private fun breedEntity(id: String, name: String) = CatBreedEntity(
        id = id,
        name = name,
        altName = null,
        imageUrl = "https://example.com/$id.jpg",
        origin = "Egypt",
        description = "$name is playful and social.",
        lifeSpan = "12 - 16",
        weight = "8 - 12",
        temperaments = "Active, Curious, Gentle",
        syncStatus = SyncStatus.SYNCED
    )

    private fun traitsEntity(breedId: String) = CatTraitsEntity(
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

    private fun badgesEntity(breedId: String) = CatBadgesEntity(
        breedId = breedId,
        isIndoor = true,
        isHypoallergenic = false,
        isHairless = false,
        hasShortLegs = false,
        isLap = true,
    )

    private fun favouriteEntity(
        id: Long,
        breedId: String,
        imageId: String,
        createdAt: Long,
    ) = FavouriteImageEntity(
        id = id,
        breedId = breedId,
        imageId = imageId,
        imageUrl = "https://cdn2.thecatapi.com/images/$imageId.jpg",
        breedName = "Breed-$breedId",
        createdAt = createdAt,
        syncStatus = SyncStatus.SYNCED
    )
}