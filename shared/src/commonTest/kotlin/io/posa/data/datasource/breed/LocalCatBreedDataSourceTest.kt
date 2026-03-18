package io.posa.data.datasource.breed

import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.sync.SyncStatus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocalCatBreedDataSourceTest {

    @Test
    fun getBreed_returnsNull_whenDaoReturnsNull() = runTest {
        val dao = FakeCatBreedDao()
        val dataSource = LocalCatBreedDataSource(catBreedDao = dao)

        val result = dataSource.getBreed(id = "abys")

        assertNull(result)
        assertEquals("abys", dao.requestedBreedId)
    }

    @Test
    fun getBreed_returnsMappedBreed_whenDaoReturnsEntityWithRelations() = runTest {
        val dao = FakeCatBreedDao().apply {
            getBreedResult = testBreedEntityWithRelations()
        }
        val dataSource = LocalCatBreedDataSource(catBreedDao = dao)

        val result = dataSource.getBreed(id = "abys")

        assertEquals(testBreedEntityWithRelations().toDomain(), result)
        assertEquals("abys", dao.requestedBreedId)
    }

    @Test
    fun insert_insertsBreedTraitsAndBadges_inExpectedOrder() = runTest {
        val dao = FakeCatBreedDao()
        val dataSource = LocalCatBreedDataSource(catBreedDao = dao)
        val breed = testBreed()

        dataSource.insert(breed = breed)

        assertEquals(CatBreedEntity.from(breed), dao.insertedBreed)
        assertEquals(CatTraitsEntity.from(traits = breed.traits, breedId = breed.id), dao.insertedTraits)
        assertEquals(CatBadgesEntity.from(badges = breed.badges, breedId = breed.id), dao.insertedBadges)
        assertEquals(listOf("insertBreed", "insertTraits", "insertBadges"), dao.callLog)
    }

    @Test
    fun deleteById_delegatesToDao() = runTest {
        val dao = FakeCatBreedDao()
        val dataSource = LocalCatBreedDataSource(catBreedDao = dao)

        dataSource.delete(id = "abys")

        assertEquals("abys", dao.deletedId)
        assertEquals(listOf("deleteId:abys"), dao.callLog)
    }

    @Test
    fun deleteByBreed_convertsBreedAndDelegatesToDao() = runTest {
        val dao = FakeCatBreedDao()
        val dataSource = LocalCatBreedDataSource(catBreedDao = dao)
        val breed = testBreed()

        dataSource.delete(breed = breed)

        assertEquals(CatBreedEntity.from(breed), dao.deletedBreed)
        assertEquals(listOf("deleteBreed"), dao.callLog)
    }

    private class FakeCatBreedDao : CatBreedDao {
        var getBreedResult: CatBreedEntityWithTraitsAndBadges? = null
        var requestedBreedId: String? = null
        var insertedBreed: CatBreedEntity? = null
        var insertedTraits: CatTraitsEntity? = null
        var insertedBadges: CatBadgesEntity? = null
        var deletedBreed: CatBreedEntity? = null
        var deletedId: String? = null
        val callLog = mutableListOf<String>()

        override suspend fun getBreed(id: String): CatBreedEntityWithTraitsAndBadges? {
            requestedBreedId = id
            return getBreedResult
        }

        override suspend fun insert(breed: CatBreedEntity) {
            callLog += "insertBreed"
            insertedBreed = breed
        }

        override suspend fun insert(traits: CatTraitsEntity): Long {
            callLog += "insertTraits"
            insertedTraits = traits
            return 1L
        }

        override suspend fun insert(badges: CatBadgesEntity): Long {
            callLog += "insertBadges"
            insertedBadges = badges
            return 1L
        }

        override suspend fun delete(breed: CatBreedEntity) {
            callLog += "deleteBreed"
            deletedBreed = breed
        }

        override suspend fun delete(id: String) {
            callLog += "deleteId:$id"
            deletedId = id
        }

        override suspend fun deleteAll() {
            callLog += "deleteAll"
        }
    }

    private fun testBreed(id: String = "abys") = CatBreed(
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
        syncStatus = SyncStatus.PENDING_SYNC
    )

    private fun testBreedEntityWithRelations(id: String = "abys") = CatBreedEntityWithTraitsAndBadges(
        breed = CatBreedEntity(
            id = id,
            name = "Abyssinian",
            altName = "Aby",
            imageUrl = "https://example.com/abys.jpg",
            origin = "Egypt",
            description = "Active and affectionate.",
            lifeSpan = "14 - 15",
            weight = "8 - 12",
            temperaments = "Active, Curious, Gentle",
            syncStatus = SyncStatus.PENDING_SYNC
        ),
        traits = CatTraitsEntity(
            breedId = id,
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
        badges = CatBadgesEntity(
            breedId = id,
            isIndoor = true,
            isHypoallergenic = false,
            isHairless = false,
            hasShortLegs = false,
            isLap = true,
        )
    )
}