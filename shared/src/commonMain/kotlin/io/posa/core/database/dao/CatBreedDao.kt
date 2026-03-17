package io.posa.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.posa.core.database.entity.breed.CatBreedEntity

@Dao
interface CatBreedDao {
    @Query("SELECT * FROM cat_breeds WHERE id = :id")
    suspend fun getBreed(id: String): CatBreedEntity?

    @Insert
    suspend fun insert(breed: CatBreedEntity): Long

    @Delete
    suspend fun delete(breed: CatBreedEntity)

    @Query("DELETE FROM cat_breeds")
    suspend fun deleteAll()
}