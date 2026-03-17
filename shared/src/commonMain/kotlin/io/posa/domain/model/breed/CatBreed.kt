package io.posa.domain.model.breed

import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel

data class CatBreed(
    val id: String,
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
    override val syncStatus: SyncStatus
) : SyncableModel

