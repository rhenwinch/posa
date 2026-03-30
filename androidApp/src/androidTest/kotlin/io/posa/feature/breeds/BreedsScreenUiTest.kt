package io.posa.feature.breeds

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.posa.core.common.UiIdentifiers
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class BreedsScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_then_success_rendersDeck() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(BreedsUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = {},
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_LOADING).assertIsDisplayed()

        rule.runOnIdle {
            uiState.value = BreedsUiState(
                deck = listOf(
                    TestFakes.sampleBreed(id = "b1"),
                    TestFakes.sampleBreed(id = "b2", name = "Abyssinian2"),
                ),
                isLoading = false,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.breedsCardTop("b1"))).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_DECK).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b1")).assertIsDisplayed()
        rule.onNodeWithContentDescription("Breed card Abyssinian").assertIsDisplayed()
    }

    @Test
    fun error_state_rendersErrorUi() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                isLoading = false,
                error = IllegalStateException("boom"),
            )
        )

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = {},
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("Something went wrong")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Something went wrong").assertIsDisplayed()
        rule.onNodeWithText("boom").assertIsDisplayed()
    }

    @Test
    fun empty_state_rendersEndOfDeckUi() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = emptyList(),
                isLoading = false,
                hasReachedEnd = true,
            )
        )

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = {},
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("You've seen all breeds!")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("You've seen all breeds!").assertIsDisplayed()
    }

    @Test
    fun swipeRight_addsToFavourites_showsSnackbar_andAdvancesDeck() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1"), TestFakes.sampleBreed(id = "b2")),
                isLoading = false,
            )
        )

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = {},
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = { breed ->
                        uiState.value = uiState.value.copy(deck = uiState.value.deck.filterNot { it.id == breed.id })
                        events.tryEmit(BreedsEvent.FavouriteAdded)
                    },
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.breedsCardTop("b1"))).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b1")).performTouchInput { swipeRight() }

        rule.waitUntil(6_000) {
            rule.onAllNodes(hasText("Added to favourites ❤️")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Added to favourites ❤️").assertIsDisplayed()
        if (rule.onAllNodes(hasTestTag(UiIdentifiers.breedsCardTop("b1"))).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected top card b1 to be removed")
        }
        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b2")).assertIsDisplayed()
    }

    @Test
    fun swipeLeft_triggersTrollSwipe_showsTrollSnackbar_andStillFavourites() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1"), TestFakes.sampleBreed(id = "b2")),
                isLoading = false,
            )
        )

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = {},
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = { breed ->
                        uiState.value = uiState.value.copy(deck = uiState.value.deck.filterNot { it.id == breed.id })
                        events.tryEmit(BreedsEvent.FavouriteAdded)
                    },
                    onSwipeLeft = { breed ->
                        uiState.value = uiState.value.copy(deck = uiState.value.deck.filterNot { it.id == breed.id })
                        events.tryEmit(BreedsEvent.DismissedButHellNah)
                    },
                    trollMessageProvider = { "troll" },
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.breedsCardTop("b1"))).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b1")).performTouchInput { swipeLeft() }

        rule.waitUntil(6_000) {
            rule.onAllNodes(hasText("troll")).fetchSemanticsNodes().isNotEmpty()
        }

        if (rule.onAllNodes(hasText("Added to favourites ❤️")).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Did not expect normal favourite snackbar on troll swipe")
        }

        if (rule.onAllNodes(hasTestTag(UiIdentifiers.breedsCardTop("b1"))).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected top card b1 to be removed")
        }
        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b2")).assertIsDisplayed()
    }

    @Test
    fun favouritesButton_canBeClicked() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1")),
                isLoading = false,
            )
        )

        var navigated = false

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    onNavigateToFavourites = { navigated = true },
                    uiState = uiState.value,
                    events = events,
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("Posa")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_TOP_BAR_FAVOURITES_BUTTON).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.BREEDS_TOP_BAR_FAVOURITES_BUTTON).performClick()

        rule.waitUntil(2_000) { navigated }
    }
}
