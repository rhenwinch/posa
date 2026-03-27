package io.posa.feature.favourites

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.espresso.Espresso.pressBack
import io.posa.core.common.enum.SortOrder
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class FavouritesScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_then_empty_state() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(FavouritesUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = {},
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        rule.onNodeWithTag("favourites:loading").assertIsDisplayed()

        rule.runOnIdle {
            uiState.value = FavouritesUiState(
                favourites = emptyList(),
                isLoading = false,
                error = null,
                sortOrder = SortOrder.DESC,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("No favourites yet")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("No favourites yet").assertIsDisplayed()
    }

    @Test
    fun error_state_rendersErrorUi() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            FavouritesUiState(
                favourites = emptyList(),
                isLoading = false,
                error = IllegalStateException("boom"),
                sortOrder = SortOrder.DESC,
            )
        )

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = {},
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasText("Couldn't load favourites")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("Couldn't load favourites").assertIsDisplayed()
        rule.onNodeWithText("boom").assertIsDisplayed()
    }

    @Test
    fun grid_scrolls_toEndLabel_and_layoutPlacesSortAboveItems() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(FavouritesUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = {},
                    onSortOrderChange = { sort ->
                        uiState.value = uiState.value.copy(sortOrder = sort)
                    },
                    events = events,
                )
            }
        }

        val items = (1..80).map { idx -> TestFakes.sampleFavourite(imageId = "img_$idx") }

        rule.runOnIdle {
            uiState.value = FavouritesUiState(
                favourites = items,
                isLoading = false,
                error = null,
                sortOrder = SortOrder.DESC,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:grid")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_1")).fetchSemanticsNodes().isNotEmpty()
        }

        val sortBounds = rule.onNodeWithTag("favourites:sortOrder").getUnclippedBoundsInRoot()
        val firstItemBounds = rule.onNodeWithTag("favourites:item:img_1").getUnclippedBoundsInRoot()

        if (sortBounds.top >= firstItemBounds.top) {
            throw AssertionError("Expected sort toggle above first grid item")
        }

        if (rule.onAllNodes(hasTestTag("favourites:endOfList")).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected end-of-list label to be offscreen initially")
        }

        rule.onNodeWithTag("favourites:grid").performScrollToNode(hasTestTag("favourites:endOfList"))

        rule.onNodeWithTag("favourites:endOfList").assertIsDisplayed()
    }

    @Test
    fun clickingCard_opensAndDismissesDetailSheet() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(FavouritesUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = {},
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        val item = TestFakes.sampleFavourite(imageId = "img_1")
        rule.runOnIdle {
            uiState.value = FavouritesUiState(
                favourites = listOf(item),
                isLoading = false,
                error = null,
                sortOrder = SortOrder.DESC,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_1")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:item:img_1").performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:detailSheet")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:detailSheet").assertIsDisplayed()
        rule.onNodeWithTag("breedDetail:root").assertIsDisplayed()

        pressBack()

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:detailSheet")).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun remove_success_showsSnackbar() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(FavouritesUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = { fav ->
                        uiState.value = uiState.value.copy(
                            favourites = uiState.value.favourites.filterNot { it.imageId == fav.imageId }
                        )
                        events.tryEmit(FavouritesEvent.FavouriteRemoved)
                    },
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        val item = TestFakes.sampleFavourite(imageId = "img_1")
        rule.runOnIdle {
            uiState.value = FavouritesUiState(
                favourites = listOf(item),
                isLoading = false,
                error = null,
                sortOrder = SortOrder.DESC,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_1:remove")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:item:img_1:remove").performClick()

        rule.waitUntil(6_000) {
            rule.onAllNodes(hasText("🤨🤨🤨⁉️")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("🤨🤨🤨⁉️").assertIsDisplayed()
    }

    @Test
    fun remove_failure_restoresItem_and_showsErrorSnackbar() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(FavouritesUiState(isLoading = true))

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = { fav ->
                        uiState.value = uiState.value.copy(
                            favourites = uiState.value.favourites.filterNot { it.imageId == fav.imageId }
                        )
                        events.tryEmit(FavouritesEvent.ShowError("nope"))
                    },
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        val item = TestFakes.sampleFavourite(imageId = "img_1")
        rule.runOnIdle {
            uiState.value = FavouritesUiState(
                favourites = listOf(item),
                isLoading = false,
                error = null,
                sortOrder = SortOrder.DESC,
            )
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_1:remove")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:item:img_1:remove").performClick()

        rule.waitUntil(6_000) {
            rule.onAllNodes(hasText("nope")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithText("nope").assertIsDisplayed()

        rule.runOnIdle {
            uiState.value = uiState.value.copy(favourites = listOf(item))
        }

        rule.waitUntil(6_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_1")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:item:img_1").assertIsDisplayed()
    }
}
