package io.posa.feature.breeds

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class BreedsAnimationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun swipeRight_animatesOff_thenInvokesCallback() {
        rule.mainClock.autoAdvance = false

        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(
                    TestFakes.sampleBreed(id = "b1"),
                    TestFakes.sampleBreed(id = "b2", name = "Abyssinian2"),
                ),
                isLoading = false,
            )
        )

        val swipeRightCalls = AtomicInteger(0)

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    uiState = uiState.value,
                    events = events,
                    onNavigateToFavourites = {},
                    onSwipeRight = { breed ->
                        swipeRightCalls.incrementAndGet()
                        uiState.value = uiState.value.copy(
                            deck = uiState.value.deck.filterNot { it.id == breed.id }
                        )
                    },
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("breeds:card:top:b1")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("breeds:card:top:b1").performTouchInput { swipeRight() }
        rule.waitForIdle()

        if (swipeRightCalls.get() != 0) throw AssertionError("Expected callback after animation, not immediately")

        rule.mainClock.advanceTimeBy(400)
        rule.waitForIdle()

        if (swipeRightCalls.get() != 1) throw AssertionError("Expected callback to be invoked after swipe animation")

        if (rule.onAllNodes(hasTestTag("breeds:card:top:b1")).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected b1 card to be removed from the deck")
        }

        rule.onNodeWithTag("breeds:card:top:b2").assertExists()
    }

    @Test
    fun swipeLeft_triggersTrollSwipeAnimation_thenInvokesOnSwipeLeft() {
        rule.mainClock.autoAdvance = false

        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(
                    TestFakes.sampleBreed(id = "b1"),
                    TestFakes.sampleBreed(id = "b2"),
                ),
                isLoading = false,
            )
        )

        val swipeLeftCalls = AtomicInteger(0)

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    uiState = uiState.value,
                    events = events,
                    onNavigateToFavourites = {},
                    onSwipeRight = {},
                    onSwipeLeft = { breed ->
                        swipeLeftCalls.incrementAndGet()
                        uiState.value = uiState.value.copy(
                            deck = uiState.value.deck.filterNot { it.id == breed.id }
                        )
                    },
                    trollMessageProvider = { "troll" },
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("breeds:card:top:b1")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("breeds:card:top:b1").performTouchInput { swipeLeft() }
        rule.waitForIdle()

        if (swipeLeftCalls.get() != 0) throw AssertionError("Expected callback after troll swipe animation, not immediately")

        rule.mainClock.advanceTimeBy(400 * 2) // Troll swipe has a longer animation duration
        rule.waitForIdle()

        if (swipeLeftCalls.get() != 1) throw AssertionError("Expected callback after troll swipe animation")

        if (rule.onAllNodes(hasTestTag("breeds:card:top:b1")).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected b1 card to be removed from the deck")
        }
    }

    @Test
    fun smallDrag_springsBack_doesNotInvokeSwipeCallbacks() {
        rule.mainClock.autoAdvance = false

        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1")),
                isLoading = false,
            )
        )

        val swipeRightCalls = AtomicInteger(0)
        val swipeLeftCalls = AtomicInteger(0)

        rule.setContent {
            AppTheme {
                BreedsScreenContent(
                    uiState = uiState.value,
                    events = events,
                    onNavigateToFavourites = {},
                    onSwipeRight = { swipeRightCalls.incrementAndGet() },
                    onSwipeLeft = { swipeLeftCalls.incrementAndGet() },
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("breeds:card:top:b1")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("breeds:card:top:b1").performTouchInput {
            down(center)
            moveBy(androidx.compose.ui.geometry.Offset(120f, 0f))
            up()
        }

        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        if (swipeRightCalls.get() != 0) throw AssertionError("Did not expect swipeRight callback for small drag")
        if (swipeLeftCalls.get() != 0) throw AssertionError("Did not expect swipeLeft callback for small drag")

        rule.onNodeWithTag("breeds:card:top:b1").assertExists()
    }
}
