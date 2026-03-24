package io.posa.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.posa.core.database.PosaDatabase
import io.posa.core.database.dao.CatBreedDao
import io.posa.core.database.dao.FavouriteImageDao
import io.posa.core.network.TheCatApiService
import io.posa.core.network.createTheCatApiService
import io.posa.core.network.ktorfitClient
import io.posa.data.datasource.breed.LocalCatBreedDataSource
import io.posa.data.datasource.breed.RemoteCatBreedDataSource
import io.posa.data.datasource.favourite.LocalFavouriteImageDataSource
import io.posa.data.datasource.favourite.RemoteFavouriteImageDataSource
import io.posa.data.repository.CatBreedRepositoryImpl
import io.posa.data.repository.FavouriteImageRepositoryImpl
import io.posa.di.database.PosaDatabaseFactory
import io.posa.di.datastore.PosaDataStoreFactory
import io.posa.domain.datasource.CatBreedDataSource
import io.posa.domain.datasource.FavouriteImageDataSource
import io.posa.domain.repository.CatBreedRepository
import io.posa.domain.repository.FavouriteImageRepository
import io.posa.domain.usecase.AddToFavourites
import io.posa.domain.usecase.GetCatBreeds
import io.posa.domain.usecase.GetFavourites
import io.posa.domain.usecase.RemoveFromFavourites
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect val platformModule: Module

expect val viewModelModule: Module

val coreModule = module {
    includes(platformModule)

    single<TheCatApiService> {
        ktorfitClient.createTheCatApiService()
    }

    single<DataStore<Preferences>> { get<PosaDataStoreFactory>().createDataStore() }

    single<PosaDatabase> {
        get<PosaDatabaseFactory>().createDatabase()
            .let { builder ->
                PosaDatabase.getDatabase(builder)
            }
    }

    single<CatBreedDao> { get<PosaDatabase>().catBreedDao }
    single<FavouriteImageDao> { get<PosaDatabase>().favouriteImageDao }
}

val dataSourceModule = module {
    includes(coreModule)

    single<CatBreedDataSource>(qualifier = named(LocalCatBreedDataSource.QUALIFIER_NAME)) {
        LocalCatBreedDataSource(catBreedDao = get<CatBreedDao>())
    }
    single<CatBreedDataSource>(qualifier = named(RemoteCatBreedDataSource.QUALIFIER_NAME)) {
        RemoteCatBreedDataSource(apiService = get<TheCatApiService>())
    }

    single<FavouriteImageDataSource>(qualifier = named(LocalFavouriteImageDataSource.QUALIFIER_NAME)) {
        LocalFavouriteImageDataSource(favouritesDao = get<FavouriteImageDao>())
    }
    single<FavouriteImageDataSource>(qualifier = named(RemoteFavouriteImageDataSource.QUALIFIER_NAME)) {
        RemoteFavouriteImageDataSource(api = get<TheCatApiService>(), dataStore = get<DataStore<Preferences>>())
    }
}

val repositoryModule = module {
    includes(dataSourceModule)

    single<FavouriteImageRepository> {
        FavouriteImageRepositoryImpl(
            local = get<FavouriteImageDataSource>(
                named(name = LocalFavouriteImageDataSource.QUALIFIER_NAME)
            ),
            remote = get<FavouriteImageDataSource>(
                named(name = RemoteFavouriteImageDataSource.QUALIFIER_NAME)
            )
        )
    }

    single<CatBreedRepository> {
        CatBreedRepositoryImpl(
            local = get<CatBreedDataSource>(
                named(name = LocalCatBreedDataSource.QUALIFIER_NAME)
            ),
            remote = get<CatBreedDataSource>(
                named(name = RemoteCatBreedDataSource.QUALIFIER_NAME)
            )
        )
    }
}

val useCaseModule = module {
    includes(repositoryModule)
    single {
        GetCatBreeds(
            catBreedRepository = get<CatBreedRepository>(),
            favouriteImageRepository = get<FavouriteImageRepository>()
        )
    }
    factory { GetFavourites(repository = get<FavouriteImageRepository>()) }
    factory { RemoveFromFavourites(repository = get<FavouriteImageRepository>()) }
    factory {
        AddToFavourites(
            favouriteRepository = get<FavouriteImageRepository>(),
            catBreedRepository = get<CatBreedRepository>()
        )
    }
}