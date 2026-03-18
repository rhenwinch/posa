package io.posa.core.database.entity.breed

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
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

data class CatBreedEntityWithTraitsAndBadges(
    @Embedded val breed: CatBreedEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "breedId"
    )
    val traits: CatTraits,
    @Relation(
        parentColumn = "id",
        entityColumn = "breedId"
    )
    val badges: CatBadges
) {
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
            traits = traits,
            badges = badges,
            syncStatus = breed.syncStatus,
            temperaments = breed.temperaments
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
        )
    }
}