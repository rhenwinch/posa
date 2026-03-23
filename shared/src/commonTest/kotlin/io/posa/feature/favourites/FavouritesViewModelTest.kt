package io.posa.feature.favourites

import app.cash.turbine.test
import io.posa.core.common.enum.SortOrder
import io.posa.core.common.enum.SyncStatus
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.favourite.FavouriteImage
import io.posa.domain.repository.FavouriteImageRepository
import io.posa.domain.usecase.GetFavourites
import io.posa.domain.usecase.RemoveFromFavourites
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FavouritesViewModelTest {

    @Test
    fun init_loadsFirstPage_andUpdatesUiState() = runViewModelTest {
        val firstPage = testFavourites(
            start = 1L,
            count = FavouritesViewModel.PAGE_SIZE,
        )
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flowOf(firstPage)
        }

        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(firstPage, state.favourites)
        assertFalse(state.isLoading)
        assertFalse(state.isPaginating)
        assertFalse(state.hasReachedEnd)
        assertEquals(null, state.error)
        assertEquals(SortOrder.DESC, state.sortOrder)

        assertEquals(listOf(0), repository.requestedPages)
        assertEquals(listOf(FavouritesViewModel.PAGE_SIZE), repository.requestedLimits)
        assertEquals(listOf(SortOrder.DESC), repository.requestedSortOrders)
    }

    @Test
    fun loadNextPage_appendsItems_andMarksEndWhenPageIsShort() = runViewModelTest {
        val firstPage = testFavourites(start = 1L, count = FavouritesViewModel.PAGE_SIZE)
        val secondPage = testFavourites(start = 100L, count = 2)
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flowOf(firstPage)
            getFavouriteFlows += flowOf(secondPage)
        }
        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(firstPage + secondPage, state.favourites)
        assertTrue(state.hasReachedEnd)
        assertFalse(state.isPaginating)
        assertEquals(listOf(0, 1), repository.requestedPages)
    }

    @Test
    fun onSortOrderChange_resetsFavourites_andFetchesFromFirstPage() = runViewModelTest {
        val initialPage = testFavourites(start = 1L, count = FavouritesViewModel.PAGE_SIZE)
        val sortedPage = listOf(testFavourite(id = 900L), testFavourite(id = 901L))
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flowOf(initialPage)
            getFavouriteFlows += flowOf(sortedPage)
        }
        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()
        viewModel.onSortOrderChange(SortOrder.ASC)

        // State resets immediately before the refresh coroutine executes.
        assertEquals(SortOrder.ASC, viewModel.uiState.value.sortOrder)
        assertTrue(viewModel.uiState.value.favourites.isEmpty())

        advanceUntilIdle()

        assertEquals(sortedPage, viewModel.uiState.value.favourites)
        assertTrue(viewModel.uiState.value.hasReachedEnd)
        assertEquals(listOf(0, 0), repository.requestedPages)
        assertEquals(listOf(SortOrder.DESC, SortOrder.ASC), repository.requestedSortOrders)
    }

    @Test
    fun removeCard_emitsFavouriteRemoved_andRemovesItemFromUi() = runViewModelTest testScope@{
        val initial = testFavourites(start = 1L, count = 3)
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flowOf(initial)
        }
        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()
        val target = initial[1]

        viewModel.events.test {
            viewModel.removeCard(target)
            this@testScope.advanceUntilIdle()

            assertEquals(FavouritesEvent.FavouriteRemoved, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(listOf(initial[0], initial[2]), viewModel.uiState.value.favourites)
        assertEquals(listOf(target), repository.removedImages)
    }

    @Test
    fun removeCard_restoresItem_andEmitsError_whenRemoveFails() = runViewModelTest testScope@{
        val initial = testFavourites(start = 1L, count = 3)
        val expected = IllegalStateException("delete failed")
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flowOf(initial)
            throwOnRemove = expected
        }
        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()
        val target = initial[1]

        viewModel.events.test {
            viewModel.removeCard(target)
            this@testScope.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is FavouritesEvent.ShowError)
            assertEquals("delete failed", event.message)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(initial, viewModel.uiState.value.favourites)
        assertTrue(repository.removedImages.isEmpty())
    }

    @Test
    fun init_setsError_whenGetFavouritesFails() = runViewModelTest {
        val expected = IllegalStateException("load favourites failed")
        val repository = FakeFavouriteImageRepository().apply {
            getFavouriteFlows += flow { throw expected }
        }
        val viewModel = FavouritesViewModel(
            getFavourites = GetFavourites(repository),
            removeFromFavourites = RemoveFromFavourites(repository),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.favourites.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isPaginating)
        assertEquals(expected, state.error)
    }

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeFavouriteImageRepository : FavouriteImageRepository {
        val getFavouriteFlows = mutableListOf<Flow<List<FavouriteImage>>>()

        val requestedPages = mutableListOf<Int>()
        val requestedLimits = mutableListOf<Int>()
        val requestedSortOrders = mutableListOf<SortOrder>()

        var throwOnRemove: Exception? = null
        val removedImages = mutableListOf<FavouriteImage>()

        override fun getFavouriteImages(
            page: Int,
            limit: Int,
            sortOrder: SortOrder,
        ): Flow<List<FavouriteImage>> {
            requestedPages += page
            requestedLimits += limit
            requestedSortOrders += sortOrder
            return getFavouriteFlows.removeFirstOrNull() ?: flowOf(emptyList())
        }

        override suspend fun addFavouriteImage(image: FavouriteImage) = Unit

        override suspend fun removeFavouriteImage(image: FavouriteImage) {
            throwOnRemove?.let { throw it }
            removedImages += image
        }

        override suspend fun synchronize() = Unit
    }

    private fun testFavourites(start: Long, count: Int): List<FavouriteImage> {
        return (start until start + count).map { id ->
            testFavourite(id = id)
        }
    }

    private fun testFavourite(id: Long) = FavouriteImage(
        id = id,
        imageId = "img-$id",
        imageUrl = "https://cdn2.thecatapi.com/images/img-$id.jpg",
        createdAt = Instant.fromEpochMilliseconds(1_700_000_000_000L + id),
        breed = testBreed(id = "breed-$id"),
        syncStatus = SyncStatus.SYNCED,
    )

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