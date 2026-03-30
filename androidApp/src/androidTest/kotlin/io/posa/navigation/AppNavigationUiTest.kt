package io.posa.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.posa.core.common.UiIdentifiers
import io.posa.feature.breeds.BreedsEvent
import io.posa.feature.breeds.BreedsScreenContent
import io.posa.feature.breeds.BreedsUiState
import io.posa.feature.favourites.FavouritesEvent
import io.posa.feature.favourites.FavouritesScreenContent
import io.posa.feature.favourites.FavouritesUiState
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class AppNavigationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun navigate_breeds_to_favourites_and_back() {
        val breedsEvents = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val favouritesEvents = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)

        val breedsUiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1")),
                isLoading = false,
            )
        )
        val favouritesUiState = mutableStateOf(
            FavouritesUiState(
                favourites = listOf(TestFakes.sampleFavourite(imageId = "img_1")),
                isLoading = false,
            )
        )

        rule.setContent {
            AppTheme {
                TestNavHost(
                    breedsUiState = breedsUiState.value,
                    breedsEvents = breedsEvents,
                    favouritesUiState = favouritesUiState.value,
                    favouritesEvents = favouritesEvents,
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.BREEDS_SCREEN)).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_TOP_BAR_FAVOURITES_BUTTON).performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.FAVOURITES_SCREEN)).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.FAVOURITES_SCREEN).assertIsDisplayed()

        rule.onNodeWithTag(UiIdentifiers.FAVOURITES_TOP_BAR_BACK_BUTTON).performClick()

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.BREEDS_SCREEN)).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_SCREEN).assertIsDisplayed()
    }
}

@Composable
private fun TestNavHost(
    breedsUiState: BreedsUiState,
    breedsEvents: MutableSharedFlow<BreedsEvent>,
    favouritesUiState: FavouritesUiState,
    favouritesEvents: MutableSharedFlow<FavouritesEvent>,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "breeds",
    ) {
        composable("breeds") {
            BreedsScreenContent(
                uiState = breedsUiState,
                events = breedsEvents,
                onNavigateToFavourites = { navController.navigate("favourites") },
                onSwipeRight = {},
                onSwipeLeft = {},
            )
        }

        composable("favourites") {
            FavouritesScreenContent(
                uiState = favouritesUiState,
                onNavigateBack = { navController.popBackStack() },
                onRemoveCard = {},
                onSortOrderChange = {},
                events = favouritesEvents,
            )
        }
    }
}
