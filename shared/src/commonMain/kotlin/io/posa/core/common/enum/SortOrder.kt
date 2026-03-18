package io.posa.core.common.enum

enum class SortOrder {
    RANDOM, ASC, DESC;

    val isAscending get() = this == ASC
    val isDescending get() = this == DESC
}