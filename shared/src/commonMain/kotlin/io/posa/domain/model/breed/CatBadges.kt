package io.posa.domain.model.breed

import io.posa.core.common.enum.SyncStatus

data class CatBadges(
    val isIndoor: Boolean,
    val isHypoallergenic: Boolean,
    val isHairless: Boolean,
    val hasShortLegs: Boolean,
    val isLap: Boolean = false,
)