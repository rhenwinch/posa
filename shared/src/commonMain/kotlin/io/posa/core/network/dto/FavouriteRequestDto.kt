package io.posa.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavouriteRequestDto(
    @SerialName("image_id") val imageId: String,
    @SerialName("sub_id") val userId: String
)