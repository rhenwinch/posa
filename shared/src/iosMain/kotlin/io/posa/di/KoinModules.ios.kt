package io.posa.di

import io.posa.core.database.PosaDatabase
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.di.database.PosaDatabaseFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
    single<PosaDatabaseFactory> { PosaDatabaseFactory() }
}