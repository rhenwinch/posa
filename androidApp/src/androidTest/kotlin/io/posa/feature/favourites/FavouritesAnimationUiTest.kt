package io.posa.feature.favourites

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.posa.domain.model.breed.CatTraits
import io.posa.feature.favourites.component.BreedDetailSheet
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class FavouritesAnimationUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun favouriteCard_removeAnimation_delaysOnRemoveCallback() {
        rule.mainClock.autoAdvance = false

        val events = MutableSharedFlow<FavouritesEvent>(extraBufferCapacity = 8)
        val uiState = mutableStateOf(
            FavouritesUiState(
                favourites = listOf(TestFakes.sampleFavourite(imageId = "img_anim")),
                isLoading = false,
            )
        )

        val removeCalls = AtomicInteger(0)

        rule.setContent {
            AppTheme {
                FavouritesScreenContent(
                    uiState = uiState.value,
                    onNavigateBack = {},
                    onRemoveCard = { fav ->
                        removeCalls.incrementAndGet()
                        uiState.value = uiState.value.copy(
                            favourites = uiState.value.favourites.filterNot { it.imageId == fav.imageId }
                        )
                    },
                    onSortOrderChange = { sort ->
                        uiState.value = uiState.value.copy(sortOrder = sort)
                    },
                    events = events,
                )
            }
        }

        rule.waitUntil(5_000) {
            rule.onAllNodes(hasTestTag("favourites:item:img_anim:remove")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("favourites:item:img_anim:remove").performClick()

        rule.mainClock.advanceTimeBy(150)
        rule.waitForIdle()
        if (removeCalls.get() != 0) throw AssertionError("Expected remove callback to be delayed by animation")

        rule.mainClock.advanceTimeBy(120)
        rule.waitForIdle()
        if (removeCalls.get() != 1) throw AssertionError("Expected remove callback after animation delay")

        if (rule.onAllNodes(hasTestTag("favourites:item:img_anim")).fetchSemanticsNodes().isNotEmpty()) {
            throw AssertionError("Expected item to be removed after delayed callback")
        }
    }

    @Test
    fun breedDetailSheet_traitProgress_animatesWhenTraitsChange() {
        rule.mainClock.autoAdvance = false

        val base = TestFakes.sampleBreed(id = "b1")
        val zeroTraits = CatTraits(
            adaptability = 0,
            affectionLevel = 0,
            childFriendly = 0,
            dogFriendly = 0,
            energyLevel = 0,
            grooming = 0,
            healthIssues = 0,
            intelligence = 0,
            sheddingLevel = 0,
            socialNeeds = 0,
            strangerFriendly = 0,
            vocalisation = 0,
        )

        val breedState = mutableStateOf(base.copy(traits = zeroTraits))

        rule.setContent {
            AppTheme {
                BreedDetailSheet(breed = breedState.value)
            }
        }

        rule.onNodeWithTag("breedDetail:root").assertIsDisplayed()

        val progressMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
        fun currentProgress(): Float {
            val nodes = rule
                .onAllNodes(
                    progressMatcher and hasAnyAncestor(hasTestTag("breedDetail:trait:adaptability")),
                    useUnmergedTree = true,
                )
                .fetchSemanticsNodes()

            if (nodes.isEmpty()) throw AssertionError("Could not find progress indicator for adaptability")

            val info = nodes.first().config[SemanticsProperties.ProgressBarRangeInfo]
            return info.current
        }

        val start = currentProgress()
        if (start != 0f) throw AssertionError("Expected initial progress to start at 0 for animation")

        rule.runOnIdle {
            breedState.value = TestFakes.sampleBreed(id = "b1")
        }

        val afterUpdate = currentProgress()
        if (afterUpdate !in 0f..1f) throw AssertionError("Unexpected progress value after update")

        rule.mainClock.advanceTimeBy(120)
        rule.waitForIdle()
        val mid = currentProgress()
        if (mid <= 0f) throw AssertionError("Expected progress to be animating upward")

        rule.mainClock.advanceTimeBy(500)
        rule.waitForIdle()
        val end = currentProgress()
        if (end < 0.90f) throw AssertionError("Expected progress to reach near target after animation")
    }
}
