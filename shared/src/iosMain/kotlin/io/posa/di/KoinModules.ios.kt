package io.posa.di

import io.posa.di.database.PosaDatabaseFactory
import io.posa.di.datastore.PosaDataStoreFactory
import org.koin.dsl.module

actual val platformModule = module {
    single<PosaDatabaseFactory> { PosaDatabaseFactory() }
    single<PosaDataStoreFactory> { PosaDataStoreFactory() }
}