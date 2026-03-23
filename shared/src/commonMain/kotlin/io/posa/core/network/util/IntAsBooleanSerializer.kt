package io.posa.core.network.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object IntAsBooleanSerializer : KSerializer<Boolean> {
    override val descriptor = PrimitiveSerialDescriptor(
        "IntAsBoolean",
        PrimitiveKind.INT
    )
    override fun serialize(encoder: Encoder, value: Boolean) =
        encoder.encodeInt(if (value) 1 else 0)

    override fun deserialize(decoder: Decoder): Boolean =
        decoder.decodeInt() != 0
}