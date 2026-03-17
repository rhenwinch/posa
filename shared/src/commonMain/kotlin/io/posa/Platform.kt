package io.posa

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform