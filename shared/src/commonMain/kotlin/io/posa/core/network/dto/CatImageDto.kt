package io.posa.core.network.dto

import io.posa.domain.model.image.CatImage
import kotlinx.serialization.Serializable

@Serializable
data class CatImageDto(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
    val breeds: List<CatBreedDto> = emptyList(),
) {
    fun toDomain() = CatImage(
        id = id,
        url = url,
        breed = breeds.first().toDomain()
    )
}