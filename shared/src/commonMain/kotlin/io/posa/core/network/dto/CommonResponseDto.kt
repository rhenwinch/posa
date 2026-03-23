package io.posa.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommonResponseDto(
    val id: Long? = null,
    val message: String
)