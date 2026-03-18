package io.posa.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.posa.core.common.AppDispatchers
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.database.entity.breed.CatBadgesEntity
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.core.database.entity.breed.CatBreedEntityWithTraitsAndBadges
import io.posa.core.database.entity.breed.CatTraitsEntity
import io.posa.core.database.entity.favourite.FavouriteImageEntity
import io.posa.di.database.PosaDatabaseConstructor
import org.koin.mp.KoinPlatformTools.synchronized
import org.koin.mp.Lockable
import kotlin.concurrent.Volatile

@Database(
    version = 1,
    entities = [
        CatBreedEntity::class,
        CatBadgesEntity::class,
        CatTraitsEntity::class,
        FavouriteImageEntity::class,
    ],
    views = [
        CatBreedEntityWithTraitsAndBadges::class,
    ],
)
@ConstructedBy(PosaDatabaseConstructor::class)
abstract class PosaDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_FILE = "posa_database.db"

        @Volatile
        private var INSTANCE: PosaDatabase? = null

        fun getDatabase(
            builder: RoomDatabase.Builder<PosaDatabase>
        ): PosaDatabase {
            return INSTANCE ?: synchronized(Lockable()) {
                builder
                    .setDriver(BundledSQLiteDriver())
                    .setQueryCoroutineContext(AppDispatchers.IO)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    abstract val catBreedDao: CatBreedDao
    abstract val favouriteImageDao: FavouriteImageDao
}

