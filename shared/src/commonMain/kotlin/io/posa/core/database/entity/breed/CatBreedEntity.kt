package io.posa.core.database.entity.breed

import androidx.room.Embedded
import androidx.room.DatabaseView
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.posa.domain.model.breed.CatBadges
import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.breed.CatTraits
import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel

@Entity(tableName = "cat_breeds")
data class CatBreedEntity(
    @PrimaryKey val id: String,
    val name: String,
    val altName: String?,
    val imageUrl: String,
    val origin: String,
    val description: String,
    val lifeSpan: String,
    val weight: String,
    val temperaments: String,
    override val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
) : SyncableModel {
    fun toDomain(
        traits: CatTraits,
        badges: CatBadges
    ): CatBreed {
        return CatBreed(
            id = id,
            name = name,
            altName = altName,
            imageUrl = imageUrl,
            origin = origin,
            description = description,
            lifeSpan = lifeSpan,
            weight = weight,
            syncStatus = syncStatus,
            traits = traits,
            badges = badges,
            temperaments = temperaments
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
        )
    }

    companion object {
        fun from(breed: CatBreed): CatBreedEntity {
            return CatBreedEntity(
                id = breed.id,
                name = breed.name,
                altName = breed.altName,
                imageUrl = breed.imageUrl,
                origin = breed.origin,
                description = breed.description,
                lifeSpan = breed.lifeSpan,
                weight = breed.weight,
                syncStatus = breed.syncStatus,
                temperaments = breed.temperaments.joinToString(",")
            )
        }
    }
}

@DatabaseView(
    value =
        """
        SELECT
            b.*,

            t.id AS traits_id,
            t.breedId AS traits_breedId,
            t.adaptability AS traits_adaptability,
            t.affectionLevel AS traits_affectionLevel,
            t.childFriendly AS traits_childFriendly,
            t.dogFriendly AS traits_dogFriendly,
            t.energyLevel AS traits_energyLevel,
            t.grooming AS traits_grooming,
            t.healthIssues AS traits_healthIssues,
            t.intelligence AS traits_intelligence,
            t.sheddingLevel AS traits_sheddingLevel,
            t.socialNeeds AS traits_socialNeeds,
            t.strangerFriendly AS traits_strangerFriendly,
            t.vocalisation AS traits_vocalisation,

            badges.id AS badges_id,
            badges.breedId AS badges_breedId,
            badges.isIndoor AS badges_isIndoor,
            badges.isHypoallergenic AS badges_isHypoallergenic,
            badges.isHairless AS badges_isHairless,
            badges.hasShortLegs AS badges_hasShortLegs,
            badges.isLap AS badges_isLap
        FROM cat_breeds b
        INNER JOIN cat_traits t ON b.id = t.breedId
        INNER JOIN cat_badges badges ON b.id = badges.breedId
        """,
    viewName = CatBreedEntityWithTraitsAndBadges.VIEW_NAME
)
data class CatBreedEntityWithTraitsAndBadges(
    @Embedded val breed: CatBreedEntity,
    @Embedded(prefix = "traits_")
    val traits: CatTraitsEntity,
    @Embedded(prefix = "badges_")
    val badges: CatBadgesEntity
) {
    companion object {
        const val VIEW_NAME = "cat_breed_with_traits_and_badges"
    }

    fun toDomain(): CatBreed {
        return CatBreed(
            id = breed.id,
            name = breed.name,
            altName = breed.altName,
            imageUrl = breed.imageUrl,
            origin = breed.origin,
            description = breed.description,
            lifeSpan = breed.lifeSpan,
            weight = breed.weight,
            traits = traits.toDomain(),
            badges = badges.toDomain(),
            syncStatus = breed.syncStatus,
            temperaments = breed.temperaments
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
        )
    }
}