package io.posa.core.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import kotlinx.coroutines.flow.Flow

interface FavouriteImageDao {
    @Query("SELECT * FROM favourite_images ORDER BY :filter DESC")
    fun getAllDescAsFlow(filter: String): Flow<List<FavouriteImageEntity>>

    @Query("SELECT * FROM favourite_images ORDER BY :filter ASC")
    fun getAllAscAsFlow(filter: String): Flow<List<FavouriteImageEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_images WHERE image_id = :imageId)")
    suspend fun isFavourite(imageId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favouriteImage: FavouriteImageEntity)

    @Query("DELETE FROM favourite_images WHERE image_id = :imageId")
    suspend fun remove(imageId: String)

    @Delete
    suspend fun remove(favouriteImage: FavouriteImageEntity)

    @Query("DELETE FROM favourite_images")
    suspend fun removeAll()
}