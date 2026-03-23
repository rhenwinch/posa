package io.posa.domain.model.breed

import io.posa.core.common.Config.IMAGE_BASE_URL
import io.posa.domain.model.image.CatImage

data class CatBreed(
    val id: String,
    val name: String,
    val altName: String?,
    val imageId: String,
    val origin: String,
    val description: String,
    val lifeSpan: String,
    val weight: String,
    val temperaments: List<String>,
    val traits: CatTraits,
    val badges: CatBadges,
) {
    val imageUrl get() = "$IMAGE_BASE_URL/$imageId.jpg"

    fun toCatImage() = CatImage(
        id = id,
        url = imageUrl,
        breed = this,
    )
}

