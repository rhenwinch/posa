package io.posa.core.database.entity.breed

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.posa.core.database.entity.breed.CatBreedEntity

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
)