package io.posa.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.domain.model.breed.CatBreed

@Dao
interface CatBreedDao {
    @Transaction
    @Query("SELECT * FROM cat_breeds WHERE id = :id")
    suspend fun getBreed(id: String): CatBreedEntityWithTraitsAndBadges?

    @Insert
    suspend fun insert(breed: CatBreedEntity)

    @Insert
    suspend fun insert(traits: CatTraitsEntity): Long

    @Insert
    suspend fun insert(badges: CatBadgesEntity): Long

    @Delete
    suspend fun delete(breed: CatBreedEntity)

    @Query("DELETE FROM cat_breeds WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM cat_breeds")
    suspend fun deleteAll()
}