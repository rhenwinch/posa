package io.posa.feature.favourites

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.posa.core.common.UiIdentifiers
import io.posa.feature.favourites.component.BreedDetailSheet
import io.posa.feature.favourites.component.FavouriteCard
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class FavouritesContentComposableUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun favouriteCard_displaysExpectedModelFields_andRemoveButton() {
        val favourite = TestFakes.sampleFavourite(
            imageId = "img_1",
            breedId = "b1",
        )

        rule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    Box(modifier = Modifier.size(220.dp)) {
                        FavouriteCard(
                            favourite = favourite,
                            onRemove = {},
                        )
                    }
                }
            }
        }

        rule.onNodeWithContentDescription(favourite.breed.name).assertIsDisplayed()
        rule.onNodeWithText(favourite.breed.name).assertIsDisplayed()
        rule.onNodeWithText(favourite.breed.origin).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.favouritesItemRemove(favourite.imageId)).assertIsDisplayed()
    }

    @Test
    fun breedDetailSheet_displaysExpectedSections_andDerivedTags() {
        val breed = TestFakes.sampleBreed(
            id = "b1",
            name = "Abyssinian",
        )

        rule.setContent {
            AppTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    BreedDetailSheet(breed = breed)
                }
            }
        }

        rule.onNodeWithTag(UiIdentifiers.BREED_DETAIL_ROOT).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.BREED_DETAIL_CONTENT).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.BREED_DETAIL_TEMPERAMENTS).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.BREED_DETAIL_TRAITS).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.BREED_DETAIL_BADGES).assertIsDisplayed()

        rule.onNodeWithTag(UiIdentifiers.breedDetailTemperament("Independent")).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.breedDetailTemperament("Active")).assertIsDisplayed()

        rule.onNodeWithTag(UiIdentifiers.breedDetailTrait("Adaptability")).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.breedDetailBadge("Lap cat")).assertIsDisplayed()
        rule.onNodeWithTag(UiIdentifiers.breedDetailBadge("Indoor")).assertIsDisplayed()
    }
}
