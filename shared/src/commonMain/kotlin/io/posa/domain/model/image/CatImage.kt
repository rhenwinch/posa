package io.posa.domain.model.image

import io.posa.domain.model.breed.CatBreed
import io.posa.domain.model.sync.SyncStatus
import io.posa.domain.model.sync.SyncableModel

data class CatImage(
    val id: String,
    val url: String,
    val breed: CatBreed
)