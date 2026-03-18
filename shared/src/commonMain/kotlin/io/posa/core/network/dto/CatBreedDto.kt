package io.pusa.network.dto

import io.posa.core.common.Config.IMAGE_BASE_URL
import io.posa.core.common.enum.Measurement
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.sync.SyncStatus
import io.pusa.network.util.IntAsBooleanSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CatBreedDto(
    val id: String,
    val name: String,
    val weightDto: WeightDto,
    val temperament: String,
    val origin: String,
    val description: String,
    val grooming: Int,
    val intelligence: Int,
    val vocalisation: Int,
    val adaptability: Int,

    @SerialName("breed_group") val breedGroup: String? = null,
    @SerialName("cfa_url") val cfaUrl: String? = null,
    @SerialName("vetstreet_url") val vetstreetUrl: String? = null,
    @SerialName("vcahospitals_url") val vcahospitalsUrl: String? = null,
    @SerialName("country_codes") val countryCodes: String,
    @SerialName("country_code") val countryCode: String,
    @SerialName("life_span") val lifeSpan: String,

    @SerialName("affection_level") val affectionLevel: Int,
    @SerialName("alt_names") val altNames: String,
    @SerialName("child_friendly") val childFriendly: Int,
    @SerialName("dog_friendly") val dogFriendly: Int,
    @SerialName("energy_level") val energyLevel: Int,
    @SerialName("health_issues") val healthIssues: Int,
    @SerialName("reference_image_id") val referenceImageId: String,
    @SerialName("shedding_level") val sheddingLevel: Int,
    @SerialName("social_needs") val socialNeeds: Int,
    @SerialName("stranger_friendly") val strangerFriendly: Int,
    @SerialName("wikipedia_url") val wikipediaUrl: String? = null,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("experimental")
    val isExperimental: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("hairless")
    val isHairless: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("natural")
    val isNatural: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("rare")
    val isRare: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    val rex: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("suppressed_tail")
    val hasSuppressedTail: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("short_legs")
    val hasShortLegs: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("hypoallergenic")
    val isHypoallergenic: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("indoor")
    val isIndoor: Boolean,

    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("lap")
    val isLap: Boolean? = null,
) {
    @Serializable
    data class WeightDto(
        val imperial: String,
        val metric: String
    )

    fun toDomain() = CatBreed(
        id = id,
        name = name,
        altName = altNames.trim().ifBlank { null },
        imageUrl = "$IMAGE_BASE_URL/$referenceImageId.jpg",
        origin = origin,
        description = description,
        lifeSpan = lifeSpan,
        syncStatus = SyncStatus.SYNCED,
        weight = weightDto.imperial,
        temperaments = temperament
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        traits = CatTraits(
            adaptability = adaptability,
            affectionLevel = affectionLevel,
            childFriendly = childFriendly,
            dogFriendly = dogFriendly,
            energyLevel = energyLevel,
            grooming = grooming,
            healthIssues = healthIssues,
            intelligence = intelligence,
            sheddingLevel = sheddingLevel,
            socialNeeds = socialNeeds,
            strangerFriendly = strangerFriendly,
            vocalisation = vocalisation
        ),
        badges = CatBadges(
            isIndoor = isIndoor,
            isHypoallergenic = isHypoallergenic,
            isHairless = isHairless,
            hasShortLegs = hasShortLegs,
            isLap = isLap ?: false,
        )
    )
}
