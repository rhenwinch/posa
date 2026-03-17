package io.posa.core.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

object AppDispatchers {
    val Default by lazy { Dispatchers.Default }
    val IO by lazy { Dispatchers.IO }
    val Main by lazy { Dispatchers.Main }
}