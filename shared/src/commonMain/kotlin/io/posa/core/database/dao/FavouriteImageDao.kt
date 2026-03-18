package io.posa.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.core.database.entity.favourite.FavouriteImageWithBreed
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteImageDao {
    @Transaction
    @Query(
        """
        SELECT * FROM favourite_images
        ORDER BY createdAt DESC
        LIMIT :limit OFFSET (:page * :limit)
        """
    )
    fun getAllDescAsFlow(
        page: Int,
        limit: Int
    ): Flow<List<FavouriteImageWithBreed>>

    @Transaction
    @Query(
        """
        SELECT * FROM favourite_images
        ORDER BY createdAt ASC
        LIMIT :limit OFFSET (:page * :limit)
        """
    )
    fun getAllAscAsFlow(
        page: Int,
        limit: Int
    ): Flow<List<FavouriteImageWithBreed>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_images WHERE imageId = :imageId)")
    suspend fun isFavourite(imageId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(favouriteImage: FavouriteImageEntity)

    @Query("DELETE FROM favourite_images WHERE id = :id")
    suspend fun remove(id: Long)

    @Delete
    suspend fun remove(favouriteImage: FavouriteImageEntity)
}