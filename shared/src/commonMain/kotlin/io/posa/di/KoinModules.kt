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
import io.posa.feature.breeds.BreedsViewModel
import io.posa.feature.favourites.FavouritesViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

expect val platformModule: Module

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

    single<CatBreedDataSource>(qualifier = qualifier(LocalCatBreedDataSource.QUALIFIER_NAME)) {
        LocalCatBreedDataSource(catBreedDao = get<CatBreedDao>())
    }
    single<CatBreedDataSource>(qualifier = qualifier(RemoteCatBreedDataSource.QUALIFIER_NAME)) {
        RemoteCatBreedDataSource(apiService = get<TheCatApiService>())
    }

    single<FavouriteImageDataSource>(qualifier = qualifier(LocalFavouriteImageDataSource.QUALIFIER_NAME)) {
        LocalFavouriteImageDataSource(favouritesDao = get<FavouriteImageDao>())
    }
    single<FavouriteImageDataSource>(qualifier = qualifier(RemoteFavouriteImageDataSource.QUALIFIER_NAME)) {
        RemoteFavouriteImageDataSource(api = get<TheCatApiService>(), dataStore = get<DataStore<Preferences>>())
    }
}

val repositoryModule = module {
    includes(coreModule, dataSourceModule)

    single<FavouriteImageRepository> {
        FavouriteImageRepositoryImpl(
            local = get<FavouriteImageDataSource>(
                qualifier(name = LocalFavouriteImageDataSource.QUALIFIER_NAME)
            ),
            remote = get<FavouriteImageDataSource>(
                qualifier(name = RemoteFavouriteImageDataSource.QUALIFIER_NAME)
            )
        )
    }

    single<CatBreedRepository> {
        CatBreedRepositoryImpl(
            local = get<CatBreedDataSource>(
                qualifier(name = LocalCatBreedDataSource.QUALIFIER_NAME)
            ),
            remote = get<CatBreedDataSource>(
                qualifier(name = RemoteCatBreedDataSource.QUALIFIER_NAME)
            )
        )
    }
}

val useCaseModule = module {
    includes(coreModule, dataSourceModule, repositoryModule)
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

val viewModelModule = module {
    includes(coreModule, dataSourceModule, repositoryModule, useCaseModule)

    viewModel {
        BreedsViewModel(
            getCatBreeds = get<GetCatBreeds>(),
            addToFavourites = get<AddToFavourites>()
        )
    }

    viewModel {
        FavouritesViewModel(
            getFavourites = get<GetFavourites>(),
            removeFromFavourites = get<RemoveFromFavourites>()
        )
    }
}

private fun qualifier(name: String) = object : Qualifier {
    override val value get() = name
}
