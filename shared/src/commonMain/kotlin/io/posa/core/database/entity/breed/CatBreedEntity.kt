package io.posa.core.database.entity.breed

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.posa.domain.model.breed.CatBadges
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
    val temperaments: List<String>,
    val traits: CatTraits,
    val badges: CatBadges,
    override val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
) : SyncableModel