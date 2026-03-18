package io.posa.data.datasource.breed

import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.model.breed.CatBreed
import org.koin.core.qualifier.Qualifier

class LocalCatBreedDataSource(
    private val catBreedDao: CatBreedDao
) : CatBreedDataSource {
    companion object {
        const val QUALIFIER_NAME = "LocalCatBreedDataSource"
    }

    override suspend fun getBreed(id: String): CatBreed? {
        return catBreedDao.getBreed(id)?.toDomain()
    }

    override suspend fun insert(breed: CatBreed) {
        catBreedDao.insert(breed = CatBreedEntity.from(breed))
        catBreedDao.insert(
            traits = CatTraitsEntity.from(
                traits = breed.traits,
                breedId = breed.id
            )
        )
        catBreedDao.insert(
            badges = CatBadgesEntity.from(
                badges = breed.badges,
                breedId = breed.id
            )
        )
    }

    override suspend fun delete(id: String) {
        catBreedDao.delete(id = id)
    }

    override suspend fun delete(breed: CatBreed) {
        catBreedDao.delete(breed = CatBreedEntity.from(breed))
    }
}