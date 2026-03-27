package io.posa.feature.breeds

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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.posa.test.TestFakes
import io.posa.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class BreedsContentComposableUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun breedCardContent_displaysExpectedModelFields() {
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
                    Box(modifier = Modifier.size(360.dp)) {
                        BreedCardContent(breed = breed)
                    }
                }
            }
        }

        rule.onNodeWithContentDescription("Abyssinian").assertIsDisplayed()
        rule.onNodeWithText("Abyssinian").assertIsDisplayed()
        rule.onNodeWithText("Egypt").assertIsDisplayed()
        rule.onNodeWithText("🕑 14 - 15 yrs").assertIsDisplayed()
        rule.onNodeWithText("Independent").assertIsDisplayed()
        rule.onNodeWithText("Active").assertIsDisplayed()
    }
}
