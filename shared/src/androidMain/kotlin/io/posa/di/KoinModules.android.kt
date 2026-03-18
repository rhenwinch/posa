package io.posa.di

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import io.posa.core.database.PosaDatabase
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.datastore.PosaDataStore
import io.posa.di.database.PosaDatabaseFactory
import io.posa.di.datastore.PosaDataStoreFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
    single<PosaDatabaseFactory> { PosaDatabaseFactory(androidContext()) }
    single<PosaDataStoreFactory> { PosaDataStoreFactory(androidContext()) }
}