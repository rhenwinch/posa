package io.pusa.network.dto

import androidx.compose.ui.util.fastMap
import io.posa.core.common.enum.Measurement
import io.posa.domain.model.image.CatImage
import io.posa.domain.model.sync.SyncStatus
import kotlinx.serialization.Serializable

@Serializable
data class CatImageDto(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val breeds: List<CatBreedDto> = emptyList(),
) {
    fun toDomain(measurement: Measurement) = CatImage(
        id = id,
        url = url,
        syncStatus = SyncStatus.SYNCED,
        breed = breeds
            .fastMap { it.toDomain(measurement) }
            .first()
    )
}