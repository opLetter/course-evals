package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// Note: we initially used getData() (and getDataDelinquent()) to get the data, but it turns out
// getFullData() was better. We keep the other functions around for now, but they're not used.

suspend fun getReportIDs(
    chunk: List<String>,
    getId: suspend (String) -> String = { getReportId(it) },
): Map<String, String> {
    return chunk.pmap { i2 ->
        flow {
            emit(i2 to getId(i2))
        }.retry(3) {
            it.printStackTrace()
            (it is HttpRequestTimeoutException || it is ConnectTimeoutException)
                .also { retry ->
                    if (!retry) return@also
                    println("D: retrying, delaying 15 seconds")
                    delay(15_000)
                }
        }.single()
            .also { delay(15000) }
    }.toMap()
}

suspend fun getFullData() {
    val terms = getTerms()

    prefixes.forEach { prefix ->
        terms.sortedDescending().chunked(terms.size / 2 + 1).forEach { chunk ->
            chunk.pmap { year ->
                println("$prefix $year")
                try {
                    val reportId = getReportIdByPrefix(prefix, year)
                    val data = getFullEntries(reportId)
                    makeFileAndDir("rawData-full/$prefix/$year.json").writeText(Json.encodeToString(data))
                } catch (e: Exception) {
                    e.printStackTrace()
                    File("rawData-full/errors.txt").appendText("$prefix $year ${e.message}\n")
                }
            }
        }
    }
}

suspend fun getData() {
    val ids = File("profIds.txt").readLines().map { it.substringAfterBefore("\"", "\"") }

    ids.chunked(20).forEachIndexed { index, chunk ->
        val reportIds = getReportIDs(chunk)
        if (index % 2 == 0) delay(30_000)
        println("Done IDs $index")
        reportIds.mapValues {
            flow {
                emit(getEntries(it.value))
            }.retry(3) {
                it.printStackTrace()
                (it is HttpRequestTimeoutException || it is ConnectTimeoutException)
                    .also { retry ->
                        if (!retry) return@also
                        println("D2: retrying, delaying 15 seconds")
                        delay(15_000)
                    }
            }.single()
        }.also {
            val file = makeFileAndDir("rawData/data$index.json")
            file.writeText(Json.encodeToString(it.toMap()))
            println("Done $index")
        }
    }
}

// Some profs don't seem to work properly, so we classify them as delinquents and handle them
// separately. We get the ideas in two separate chunks of semesters, as this works for some reason.
// This is a temporary solution, and we should find a better way to handle this.
suspend fun getDataDelinquent() {
    val ids = File("profIds.txt").readLines().map { it.substringAfterBefore("\"", "\"") }

    ids.chunked(20).forEachIndexed { index, chunk ->
        val delinquents = listOf(82, 126, 180, 190, 442, 512, 513, 515, 519, 539)
        if (index !in delinquents) return@forEachIndexed
        val reportIdsA = getReportIDs(chunk) { getReportId(it, startTerm = "200508", endTerm = "200905") }
        val reportIdsB = getReportIDs(chunk) { getReportId(it, startTerm = "200908", endTerm = "201401") }
        val reportIdsC = getReportIDs(chunk) { getReportId(it, startTerm = "201405", endTerm = "202208") }
        val reportIds = reportIdsA.mapValues { listOf(it.value, reportIdsB[it.key]!!, reportIdsC[it.key]!!) }
        println("Done IDs $index")
        reportIds.mapValues {
            delay(15000)
            flow {
                println("key: ${it.key}, value: ${it.value}")
                emit(it.value.flatMap { getEntries(it) })
            }.retry(3) {
                it.printStackTrace()
                (it is HttpRequestTimeoutException || it is ConnectTimeoutException)
                    .also { retry ->
                        if (!retry) return@also
                        println("D2: retrying, delaying 15 seconds")
                        delay(15_000)
                    }
            }.single()
        }.also {
            val file = makeFileAndDir("data-2/data$index.json")
            file.writeText(Json.encodeToString(it.toMap()))
            println("Done $index")
        }
    }
}
