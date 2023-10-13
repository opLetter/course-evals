package io.github.opletter.courseevals.common.remote

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*

val DefaultClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }

    // try to avoid timeouts by default
    install(HttpTimeout) {
        connectTimeoutMillis = 100_000
    }
    engine {
        requestTimeout = 150_000
    }
}