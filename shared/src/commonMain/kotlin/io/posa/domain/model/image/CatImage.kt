package io.posa.domain.model.image

import io.posa.domain.model.breed.CatBreed
import io.posa.core.common.enum.SyncStatus

data class CatImage(
    val id: String,
    val url: String,
    val breed: CatBreed
)