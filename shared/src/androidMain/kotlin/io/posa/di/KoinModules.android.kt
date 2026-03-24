package io.posa.di

import io.posa.di.database.PosaDatabaseFactory
import io.posa.di.datastore.PosaDataStoreFactory
import io.posa.domain.usecase.AddToFavourites
import io.posa.domain.usecase.GetCatBreeds
import io.posa.domain.usecase.GetFavourites
import io.posa.domain.usecase.RemoveFromFavourites
import io.posa.feature.breeds.BreedsViewModel
import io.posa.feature.favourites.FavouritesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

actual val platformModule = module {
    single<PosaDatabaseFactory> { PosaDatabaseFactory(androidContext()) }
    single<PosaDataStoreFactory> { PosaDataStoreFactory(androidContext()) }
}

actual val viewModelModule = module {
    includes(useCaseModule)

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