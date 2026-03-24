package io.posa.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.core.database.entity.breed.CatTraitsEntity

@Dao
interface CatBreedDao {
    @Query("SELECT * FROM ${CatBreedEntityWithTraitsAndBadges.VIEW_NAME} WHERE id = :id")
    suspend fun getBreed(id: String): CatBreedEntityWithTraitsAndBadges?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(breed: CatBreedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(traits: CatTraitsEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(badges: CatBadgesEntity): Long

    @Delete
    suspend fun delete(breed: CatBreedEntity)

    @Query("DELETE FROM cat_breeds WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM cat_breeds")
    suspend fun deleteAll()
}