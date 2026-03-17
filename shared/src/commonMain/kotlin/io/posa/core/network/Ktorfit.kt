package io.posa.core.network

import de.jensklingenberg.ktorfit.ktorfit
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.serialization.kotlinx.json.json
import io.posa.core.common.BuildKonfig
import io.posa.core.common.Config
import io.posa.core.network.util.addApiKey
import kotlinx.serialization.json.Json

val ktorfitClient by lazy {
    ktorfit {
        baseUrl(Config.BASE_URL)
        httpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }.also {
        it.httpClient.plugin(HttpSend).intercept {
            addApiKey(it)
        }
    }
}