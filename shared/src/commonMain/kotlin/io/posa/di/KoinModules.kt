package io.posa.di

import io.posa.core.database.PosaDatabase
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.network.ktorfitClient
import io.posa.data.datasource.breed.LocalCatBreedDataSource
import io.posa.data.datasource.breed.RemoteCatBreedDataSource
import io.posa.di.database.PosaDatabaseFactory
import io.posa.domain.datasource.CatBreedDataSource
import io.pusa.network.TheCatApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    includes(
        coreModules,
        dataSourceModules
    )
}

val coreModules = module {
    includes(platformModule)

    single<TheCatApiService> {
        ktorfitClient.create<TheCatApiService>()
    }

    single<PosaDatabase> {
        get<PosaDatabaseFactory>().createDatabase()
            .let { builder ->
                PosaDatabase.getDatabase(builder)
            }
    }

    single<CatBreedDao> { get<PosaDatabase>().catBreedDao }
    single<FavouriteImageDao> { get<PosaDatabase>().favouriteImageDao }
}

val dataSourceModules = module {
    includes(coreModules)

    single<CatBreedDataSource>(qualifier = qualifier(LocalCatBreedDataSource.QUALIFIER_NAME)) {
        LocalCatBreedDataSource(catBreedDao = get<CatBreedDao>())
    }
    single<CatBreedDataSource>(qualifier = qualifier(RemoteCatBreedDataSource.QUALIFIER_NAME)) {
        RemoteCatBreedDataSource(apiService = get<TheCatApiService>())
    }
}

private fun qualifier(name: String) = object : Qualifier {
    override val value get() = name
}
