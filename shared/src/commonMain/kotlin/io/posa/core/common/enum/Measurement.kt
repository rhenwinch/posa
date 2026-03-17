package io.posa.core.common.enum

enum class Measurement {
    IMPERIAL, METRIC;

    val isImperial get() = this == IMPERIAL
    fun toggle() = if (isImperial) METRIC else IMPERIAL
}