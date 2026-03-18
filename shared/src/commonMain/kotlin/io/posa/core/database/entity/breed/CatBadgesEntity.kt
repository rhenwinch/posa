package io.posa.core.database.entity.breed

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.posa.core.database.entity.breed.CatBreedEntity
import io.posa.domain.model.breed.CatBadges

@Entity(
    tableName = "cat_badges",
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
    ],
)
data class CatBadgesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val breedId: String,
    val isIndoor: Boolean,
    val isHypoallergenic: Boolean,
    val isHairless: Boolean,
    val hasShortLegs: Boolean,
    val isLap: Boolean = false,
) {
    fun toDomain(): CatBadges {
        return CatBadges(
            isIndoor = isIndoor,
            isHypoallergenic = isHypoallergenic,
            isHairless = isHairless,
            hasShortLegs = hasShortLegs,
            isLap = isLap
        )
    }

    companion object {
        fun from(badges: CatBadges, breedId: String): CatBadgesEntity {
            return CatBadgesEntity(
                breedId = breedId,
                isIndoor = badges.isIndoor,
                isHypoallergenic = badges.isHypoallergenic,
                isHairless = badges.isHairless,
                hasShortLegs = badges.hasShortLegs,
                isLap = badges.isLap
            )
        }
    }
}