package io.github.opletter.courseevals.common.remote

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*

actual val ktorClient = HttpClient(CIO) {
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }

    //These data.next two seem to be required when making a lot of requests
    //Number values chosen arbitrarily, perhaps there's a better way?
    install(HttpTimeout) {
        connectTimeoutMillis = 100000
    }
    engine {
        requestTimeout = 150000
    }
}