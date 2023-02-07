package io.github.opletter.courseevals.fsu.remote

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class FSURepository(key: String? = null) {
    val cookie: String

    init {
        cookie = key ?: runBlocking {
            HttpClient { followRedirects = false }
                .get("https://fsu.evaluationkit.com/Report/Public")
                .headers.toString()
                .let {
                    val cookieKey = "ASP.NET_SessionId="
                    cookieKey + it.substringAfterBefore(cookieKey, " ")
                }.also { println(it) }
        }
    }

    private val client = HttpClient(CIO) {
        engine {
            endpoint {
                connectAttempts = 5
            }
        }
//        install(HttpRequestRetry) {
//            retryOnException(maxRetries = 5, retryOnTimeout = true)
//            exponentialDelay()
//        }
        install(HttpTimeout) {
            connectTimeoutMillis = 15000
            requestTimeoutMillis = 120000 //?
        }
        followRedirects = false
        install(Logging) {
            logger = Logger.SIMPLE
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            header("Cookie", cookie)
        }
    }

    suspend fun login(): String {
        val param = "3DmbuNmXaPhUXKr4d7FOtAo8PcT3dHnl6vSswkzeYZ43uZke1%2bRiq%2bHDlbg07k3A"
        return client.get("https://fsu.evaluationkit.com/Login/ReportPublic?id=$param").body()
    }

    suspend fun getReports(
        course: String = "",
        instructor: String = "",
        search: Boolean = true,
        page: Int = 1,
        questionKey: String = "108347-0",
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
    ): FSUResponse {
        return client.get("https://fsu.evaluationkit.com/AppApi/Report/PublicReport") {
            parameter("Course", course)
            parameter("Instructor", instructor)
            parameter("Search", search)
            parameter("page", page)
            parameter("QuestionKey", questionKey)
            parameter("AreaId", areaId)
            parameter("Sort", sort)
        }.body()
    }

    suspend fun getAllReports(
        course: String = "",
        instructor: String = "",
        search: Boolean = true,
        page: Int = 1,
        questionKey: String = "108347-0",
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
    ): List<String> {
        val response = getReports(course, instructor, search, page, questionKey, areaId, sort)
        return response.results + if (response.hasMore) {
            getAllReports(course, instructor, search, page + 1, questionKey, areaId, sort)
        } else emptyList()
    }

    // rewrite getAllReports so that it uses binary search to find the last page and then calls getReports asynchronously
    suspend fun getAllReportsAsync(
        course: String = "",
        instructor: String = "",
        search: Boolean = true,
        questionKey: String = "108347-0",
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
        startPage: Int = 1,
    ): List<String> {
        var pageCount = 10
        while (getReports(course, instructor, search, pageCount, questionKey, areaId, sort).hasMore) {
            pageCount += 10
        }
        println("pageCount: $pageCount")
        return (startPage..pageCount).chunked(150)
            .flatMap { chunk ->
                chunk.flatMap { page ->
                    getReports(course, instructor, search, page, questionKey, areaId, sort).results
                }
            }
    }

    private suspend fun List<String>.getPdfUrl(): String {
        require(size == 4) { "ids must be a list of 4 strings" }
        return client.get("https://fsu.evaluationkit.com/Reports/SRPdf.aspx?${joinToString(",")}")
            .body<String>().let {
                "https://fsu.evaluationkit.com/${it.substringAfterBefore("'../", "'")}"
            }
    }

    private suspend fun getBytes(url: String): ByteArray {
        return client.get(url).body()
    }

    suspend fun getPdfBytes(ids: List<String>): ByteArray {
        return getBytes(ids.getPdfUrl())
    }
}

// need connectionTimeout = 5000, connectAttempts = 5
suspend fun FSURepository.getAllValidCourseKeys(): List<String> {
    val alphabet = ('a'..'z')
    val searchKeys = alphabet.flatMap { first ->
        alphabet.flatMap { second ->
            alphabet.map { third -> "$first$second$third" }
        }
    }.flatMap { word ->
        (1..8).map { "$word$it" } // note: no existence of 0 or 9 as of 02/23, but that could change
    }

    val validKeys = searchKeys.chunked(10000).flatMap { subList ->
        val res = subList.chunked(150)
            .flatMap { strings ->
                strings.pmap { it to getReports(course = it).results.isNotEmpty() }
            }.mapNotNull { (key, present) ->
                key.takeIf { present }
            }.also { println(it) }
        delay(10000L)
        res
    }
    println(validKeys.joinToString("\", \"", "\"", "\","))
    return validKeys
}