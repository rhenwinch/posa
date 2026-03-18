package io.posa.core.database.entity.breed

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.posa.domain.model.breed.CatTraits

@Entity(
    tableName = "cat_traits",
    foreignKeys = [
        ForeignKey(
            entity = CatBreedEntity::class,
            parentColumns = ["id"],
            childColumns = ["breedId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["breedId"])
    ]
)
data class CatTraitsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val breedId: String,
    val adaptability: Int,
    val affectionLevel: Int,
    val childFriendly: Int,
    val dogFriendly: Int,
    val energyLevel: Int,
    val grooming: Int,
    val healthIssues: Int,
    val intelligence: Int,
    val sheddingLevel: Int,
    val socialNeeds: Int,
    val strangerFriendly: Int,
    val vocalisation: Int
) {
    fun toDomain(): CatTraits {
        return CatTraits(
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
        )
    }

    companion object {
        fun from(traits: CatTraits, breedId: String): CatTraitsEntity {
            return CatTraitsEntity(
                breedId = breedId,
                adaptability = traits.adaptability,
                affectionLevel = traits.affectionLevel,
                childFriendly = traits.childFriendly,
                dogFriendly = traits.dogFriendly,
                energyLevel = traits.energyLevel,
                grooming = traits.grooming,
                healthIssues = traits.healthIssues,
                intelligence = traits.intelligence,
                sheddingLevel = traits.sheddingLevel,
                socialNeeds = traits.socialNeeds,
                strangerFriendly = traits.strangerFriendly,
                vocalisation = traits.vocalisation
            )
        }
    }
}