package io.posa.core.network.util

import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.Sender
import io.ktor.client.request.HttpRequestBuilder
import io.posa.core.common.BuildKonfig

suspend fun Sender.addApiKey(request: HttpRequestBuilder): HttpClientCall {
    val apiHeaderKey = "x-api-key"
    if (request.headers.contains(apiHeaderKey))
        return execute(request)

    request.headers.append(apiHeaderKey, BuildKonfig.CAT_API_KEY)
    return execute(request)
}