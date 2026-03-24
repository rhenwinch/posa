package io.posa.di

import io.posa.feature.breeds.BreedsViewModel
import io.posa.feature.favourites.FavouritesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class IosViewModelProvider : KoinComponent {
    val breedsViewModel by inject<BreedsViewModel>()
    val favouritesViewModel by inject<FavouritesViewModel>()
}