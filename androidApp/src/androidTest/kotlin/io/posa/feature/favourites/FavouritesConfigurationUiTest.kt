package io.posa.feature.favourites

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.posa.core.common.enum.SortOrder
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class FavouritesConfigurationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun darkMode_rendersFavouritesScreenContent() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            FavouritesUiState(
                favourites = listOf(TestFakes.sampleFavourite(imageId = "img_1")),
                isLoading = false,
                sortOrder = SortOrder.DESC,
            )
        )

        rule.setContent {
            AppTheme(isDarkTheme = true) {
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
            rule.onAllNodes(hasTestTag("favourites:screen")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:screen").assertIsDisplayed()
        rule.onNodeWithTag("favourites:grid").assertIsDisplayed()
        rule.onNodeWithTag("favourites:item:img_1").assertIsDisplayed()
    }

    @Test
    fun orientationChange_keepsFavouritesScreenContentDisplayed() {
        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            FavouritesUiState(
                favourites = listOf(TestFakes.sampleFavourite(imageId = "img_1")),
                isLoading = false,
                sortOrder = SortOrder.DESC,
            )
        )

        rule.setContent {
            AppTheme(isDarkTheme = false) {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = {},
                    onSortOrderChange = {},
                    events = events,
                )
            }
        }

        rule.onNodeWithTag("favourites:screen").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitForIdle()

        rule.onNodeWithTag("favourites:screen").assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        rule.waitForIdle()

        rule.onNodeWithTag("favourites:screen").assertIsDisplayed()
    }
}
