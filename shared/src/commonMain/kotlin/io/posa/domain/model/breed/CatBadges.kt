package io.posa.domain.model.breed

import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel

data class CatBadges(
    val isIndoor: Boolean,
    val isHypoallergenic: Boolean,
    val isHairless: Boolean,
    val hasShortLegs: Boolean,
    val isLap: Boolean = false,
)