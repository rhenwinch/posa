package io.posa.feature.favourites.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.theme.AppTheme

@Composable
internal fun BreedDetailSheet(breed: CatBreed) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        HeroImage(imageUrl = breed.imageUrl)

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BreedHeaderSection(breed)
            TemperamentsSection(breed.temperaments)
            Text(
                text = breed.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            TraitsSection(breed.traits)
            HorizontalDivider()
            BadgesSection(breed.badges)
        }
    }
}

@Composable
private fun HeroImage(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    )
}

@Composable
private fun BreedHeaderSection(breed: CatBreed) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = breed.name,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "${breed.origin} · ${breed.lifeSpan} yrs · ${breed.weight} kg",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TemperamentsSection(temperaments: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        temperaments.forEach { tag ->
            SuggestionChip(
                onClick = {},
                label = {
                    Text(text = tag, style = MaterialTheme.typography.labelSmall)
                },
            )
        }
    }
}

@Composable
private fun TraitsSection(traits: CatTraits) {
    val rows = remember(traits) {
        listOf(
            "Adaptability" to traits.adaptability,
            "Affection" to traits.affectionLevel,
            "Child friendly" to traits.childFriendly,
            "Dog friendly" to traits.dogFriendly,
            "Energy level" to traits.energyLevel,
            "Intelligence" to traits.intelligence,
            "Shedding" to traits.sheddingLevel,
            "Vocalisation" to traits.vocalisation,
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Traits")
        rows.forEach { (name, value) ->
            TraitRow(name = name, value = value)
        }
    }
}

@Composable
private fun TraitRow(name: String, value: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = value / 5f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "trait_$name"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp)
        )
        LinearProgressIndicator(
            progress = { animatedProgress },
            drawStopIndicator = {},
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.onSurface,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(
            text = "$value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.width(16.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BadgesSection(badges: CatBadges) {
    val items = remember(badges) {
        listOf(
            "Lap cat" to badges.isLap,
            "Hypoallergenic" to badges.isHypoallergenic,
            "Hairless" to badges.isHairless,
            "Short legs" to badges.hasShortLegs,
            "Indoor" to badges.isIndoor,
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionLabel("Badges")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { (label, active) ->
                BadgePill(label = label, active = active)
            }
        }
    }
}

@Composable
private fun BadgePill(label: String, active: Boolean) {
    val containerColor = if (active)
        MaterialTheme.colorScheme.surfaceVariant
    else
        Color.Transparent

    SuggestionChip(
        onClick = {},
        label = {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor
        ),
        modifier = Modifier.heightIn(min = 25.dp)
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 0.08.em,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun BreedDetailSheetPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            BreedDetailSheet(
                breed = CatBreed(
                    id = "1",
                    name = "Abyssinian",
                    origin = "Egypt",
                    lifeSpan = "14 - 15",
                    weight = "3 - 5",
                    altName = "Abys",
                    imageId = "0XYvRd7oD",
                    description = "The Abyssinian is a breed of domestic short-haired cat with a distinctive ticked tabby coat. They are active, playful, and highly intelligent cats that enjoy human companionship.",
                    temperaments = listOf(
                        "Active",
                        "Energetic",
                        "Independent",
                        "Intelligent",
                        "Gentle"
                    ),
                    traits = CatTraits(
                        adaptability = 5,
                        affectionLevel = 5,
                        childFriendly = 3,
                        dogFriendly = 4,
                        energyLevel = 5,
                        intelligence = 5,
                        sheddingLevel = 2,
                        vocalisation = 1,
                        grooming = 1,
                        healthIssues = 2,
                        socialNeeds = 4,
                        strangerFriendly = 4
                    ),
                    badges = CatBadges(
                        isLap = true,
                        isHypoallergenic = false,
                        isHairless = false,
                        hasShortLegs = false,
                        isIndoor = true,
                    ),
                )
            )
        }
    }
}