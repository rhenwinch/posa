package io.posa.feature.breeds

import app.cash.turbine.test
import io.posa.core.common.enum.SortOrder
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.repository.CatBreedRepository
import io.posa.domain.repository.FavouriteImageRepository
import io.posa.domain.usecase.AddToFavourites
import io.posa.domain.usecase.GetCatBreeds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BreedsViewModelTest {

    @Test
    fun init_loadsFirstPage_andUpdatesUiState() = runViewModelTest {
        val firstPage = testBreeds(prefix = "random", count = BreedsViewModel.PAGE_SIZE)
        val breedRepository = FakeCatBreedRepository().apply {
            getBreedsResponses += firstPage
        }
        val favouriteRepository = FakeFavouriteImageRepository()

        val viewModel = BreedsViewModel(
            getCatBreeds = GetCatBreeds(breedRepository),
            addToFavourites = AddToFavourites(
                favouriteRepository = favouriteRepository,
                catBreedRepository = breedRepository,
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(firstPage, state.deck)
        assertFalse(state.isLoading)
        assertFalse(state.isPrefetching)
        assertFalse(state.hasReachedEnd)
        assertEquals(null, state.error)
        assertEquals(SortOrder.RANDOM, state.sortOrder)

        assertEquals(listOf(0), breedRepository.requestedPages)
        assertEquals(listOf(BreedsViewModel.PAGE_SIZE), breedRepository.requestedLimits)
        assertEquals(listOf(SortOrder.RANDOM), breedRepository.requestedSortOrders)
    }

    @Test
    fun onSortOrderChange_resetsDeck_andFetchesFromPageZeroForNewOrder() = runViewModelTest {
        val randomPage = testBreeds(prefix = "random", count = BreedsViewModel.PAGE_SIZE)
        val ascPage = listOf(testBreed(id = "asc-1"), testBreed(id = "asc-2"))

        val breedRepository = FakeCatBreedRepository().apply {
            getBreedsResponses += randomPage
            getBreedsResponses += ascPage
        }
        val favouriteRepository = FakeFavouriteImageRepository()

        val viewModel = BreedsViewModel(
            getCatBreeds = GetCatBreeds(breedRepository),
            addToFavourites = AddToFavourites(
                favouriteRepository = favouriteRepository,
                catBreedRepository = breedRepository,
            ),
        )

        advanceUntilIdle()
        viewModel.onSortOrderChange(SortOrder.ASC)

        // State resets immediately before the new fetch coroutine executes.
        assertEquals(SortOrder.ASC, viewModel.uiState.value.sortOrder)
        assertTrue(viewModel.uiState.value.deck.isEmpty())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ascPage, state.deck)
        assertTrue(state.hasReachedEnd)
        assertFalse(state.isLoading)
        assertFalse(state.isPrefetching)

        assertEquals(listOf(0, 0), breedRepository.requestedPages)
        assertEquals(
            listOf(BreedsViewModel.PAGE_SIZE, BreedsViewModel.PAGE_SIZE),
            breedRepository.requestedLimits,
        )
        assertEquals(listOf(SortOrder.RANDOM, SortOrder.ASC), breedRepository.requestedSortOrders)
    }

    @Test
    fun init_setsError_whenGetCatBreedsFails() = runViewModelTest {
        val expected = IllegalStateException("load breeds failed")
        val breedRepository = FakeCatBreedRepository().apply {
            throwOnGetBreeds = expected
        }
        val viewModel = BreedsViewModel(
            getCatBreeds = GetCatBreeds(breedRepository),
            addToFavourites = AddToFavourites(
                favouriteRepository = FakeFavouriteImageRepository(),
                catBreedRepository = breedRepository,
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.deck.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isPrefetching)
        assertEquals(expected, state.error)
    }

    @Test
    fun swipeRight_emitsFavouriteAdded_andRemovesCard() = runViewModelTest testScope@{
        val firstPage = listOf(testBreed(id = "abys"))
        val breedRepository = FakeCatBreedRepository().apply {
            getBreedsResponses += firstPage
        }
        val favouriteRepository = FakeFavouriteImageRepository()
        val viewModel = BreedsViewModel(
            getCatBreeds = GetCatBreeds(breedRepository),
            addToFavourites = AddToFavourites(
                favouriteRepository = favouriteRepository,
                catBreedRepository = breedRepository,
            ),
        )

        advanceUntilIdle()
        val target = firstPage.single()

        viewModel.events.test {
            viewModel.swipeRight(target)
            this@testScope.advanceUntilIdle()

            assertEquals(BreedsEvent.FavouriteAdded, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.deck.isEmpty())
        assertEquals(listOf(target), breedRepository.insertedBreeds)
        assertEquals(1, favouriteRepository.addedImages.size)
        assertEquals(target.id, favouriteRepository.addedImages.single().imageId)
    }

    @Test
    fun swipeRight_emitsShowError_whenAddToFavouritesFails() = runViewModelTest testScope@{
        val firstPage = listOf(testBreed(id = "beng"))
        val expected = IllegalStateException("save failed")
        val breedRepository = FakeCatBreedRepository().apply {
            getBreedsResponses += firstPage
        }
        val favouriteRepository = FakeFavouriteImageRepository().apply {
            throwOnAdd = expected
        }
        val viewModel = BreedsViewModel(
            getCatBreeds = GetCatBreeds(breedRepository),
            addToFavourites = AddToFavourites(
                favouriteRepository = favouriteRepository,
                catBreedRepository = breedRepository,
            ),
        )

        advanceUntilIdle()
        val target = firstPage.single()

        viewModel.events.test {
            viewModel.swipeRight(target)
            this@testScope.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is BreedsEvent.ShowError)
            assertEquals("save failed", event.message)
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.deck.isEmpty())
        assertEquals(listOf(target), breedRepository.insertedBreeds)
        assertTrue(favouriteRepository.addedImages.isEmpty())
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeCatBreedRepository : CatBreedRepository {
        val getBreedsResponses = mutableListOf<List<CatBreed>>()
        var throwOnGetBreeds: Throwable? = null

        val requestedPages = mutableListOf<Int>()
        val requestedLimits = mutableListOf<Int>()
        val requestedSortOrders = mutableListOf<SortOrder>()

        val insertedBreeds = mutableListOf<CatBreed>()

        override suspend fun getBreeds(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): List<CatBreed> {
            requestedPages += page
            requestedLimits += limit
            requestedSortOrders += sortOrder

            throwOnGetBreeds?.let { throw it }
            return getBreedsResponses.removeFirstOrNull() ?: emptyList()
        }

        override suspend fun getBreed(id: String): CatBreed? = null

        override suspend fun insert(breed: CatBreed) {
            insertedBreeds += breed
        }

        override suspend fun delete(breed: CatBreed) = Unit
    }

    private class FakeFavouriteImageRepository : FavouriteImageRepository {
        var throwOnAdd: Exception? = null
        val addedImages = mutableListOf<FavouriteImage>()

        override fun getFavouriteImages(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Flow<List<FavouriteImage>> = flowOf(emptyList())

        override suspend fun addFavouriteImage(image: FavouriteImage) {
            throwOnAdd?.let { throw it }
            addedImages += image
        }

        override suspend fun removeFavouriteImage(image: FavouriteImage) = Unit

        override suspend fun synchronize() = Unit
    }

    private fun testBreeds(prefix: String, count: Int): List<CatBreed> {
        return (1..count).map { index ->
            testBreed(id = "$prefix-$index")
        }
    }

    private fun testBreed(id: String) = CatBreed(
        id = id,
        name = "Breed-$id",
        altName = null,
        imageId = id,
        origin = "Egypt",
        description = "Friendly and social.",
        lifeSpan = "12 - 16",
        weight = "8 - 12",
        temperaments = listOf("Active", "Curious"),
        traits = CatTraits(
            adaptability = 5,
            affectionLevel = 5,
            childFriendly = 4,
            dogFriendly = 4,
            energyLevel = 5,
            grooming = 2,
            healthIssues = 2,
            intelligence = 5,
            sheddingLevel = 3,
            socialNeeds = 4,
            strangerFriendly = 4,
            vocalisation = 3,
        ),
        badges = CatBadges(
            isIndoor = true,
            isHypoallergenic = false,
            isHairless = false,
            hasShortLegs = false,
            isLap = false,
        ),
    )

}