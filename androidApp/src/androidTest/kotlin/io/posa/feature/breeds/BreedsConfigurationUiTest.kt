package io.posa.feature.breeds

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.posa.core.common.UiIdentifiers
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test

class BreedsConfigurationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun darkMode_rendersBreedsScreenContent() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1")),
                isLoading = false,
            )
        )

        rule.setContent {
            AppTheme(isDarkTheme = true) {
                BreedsScreenContent(
                    uiState = uiState.value,
                    events = events,
                    onNavigateToFavourites = {},
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag(UiIdentifiers.BREEDS_SCREEN)).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.breedsCardTop("b1")).assertIsDisplayed()
    }

    @Test
    fun orientationChange_keepsBreedsScreenContentDisplayed() {
        val events = MutableSharedFlow<BreedsEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            BreedsUiState(
                deck = listOf(TestFakes.sampleBreed(id = "b1")),
                isLoading = false,
            )
        )

        rule.setContent {
            AppTheme(isDarkTheme = false) {
                BreedsScreenContent(
                    uiState = uiState.value,
                    events = events,
                    onNavigateToFavourites = {},
                    onSwipeRight = {},
                    onSwipeLeft = {},
                )
            }
        }

        rule.onNodeWithTag(UiIdentifiers.BREEDS_SCREEN).assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitForIdle()

        rule.onNodeWithTag(UiIdentifiers.BREEDS_SCREEN).assertIsDisplayed()

        rule.runOnUiThread {
            rule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        rule.waitForIdle()

        rule.onNodeWithTag(UiIdentifiers.BREEDS_SCREEN).assertIsDisplayed()
    }
}
