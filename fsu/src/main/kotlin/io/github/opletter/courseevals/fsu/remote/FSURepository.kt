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
    private val cookie: String

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
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            requestTimeoutMillis = 180000
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
        termId: String = "",
        search: Boolean = true,
        page: Int = 1,
        questionKey: String = "108347-0",
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
    ): FSUResponse {
        require(course.length > 1 || instructor.length > 1) { "course or instructor must be at least 2 characters" }
        return client.get("https://fsu.evaluationkit.com/AppApi/Report/PublicReport") {
            parameter("Course", course)
            parameter("Instructor", instructor)
            parameter("TermId", termId)
            parameter("AreaId", areaId)
            parameter("QuestionKey", questionKey)
            parameter("Search", search)
            parameter("page", page)
            parameter("Sort", sort)
        }.body()
    }

    // up to 2022 Fall
    private val terms = listOf(
        "2554", "2577", "2799", "2800", "2801", "2802", "2803", "2805", "2829", "2856", "3045", "3052", "3520",
        "3793", "4148", "4410", "4780", "5131", "5694", "5884", "6647", "7135", "7418", "7433", "7453", "7466", "7477"
    )

    suspend fun getAllReports(
        course: String = "",
        instructor: String = "",
        search: Boolean = true,
        questionKey: String = "", // 108347-0
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
        startPage: Int = 1,
    ): List<String> {
        return terms.pmap { term ->
            val getReportsPage: suspend (Int) -> FSUResponse = { page: Int ->
                getReports(course, instructor, term, search, page, questionKey, areaId, sort)
            }
            getReports(course, instructor, term, search, 1, questionKey, areaId, sort)
            val pageCount = generateSequence(5) { it + (if (it < 15) 5 else 10) }
                .first { !getReportsPage(it).hasMore }
            (startPage..pageCount).pmap { getReportsPage(it).results }.flatten()
        }.flatten().also { println("count: ${it.size}") }
    }

    private suspend fun List<String>.getPdfUrl(): String {
        require(size == 4) { "ids must be a list of 4 strings" }
        return client.get("https://fsu.evaluationkit.com/Reports/SRPdf.aspx?${joinToString(",")}")
            .body<String>().let {
                "https://fsu.evaluationkit.com/${it.substringAfterBefore("'../", "'")}"
            }
    }

    suspend fun getPdfBytes(ids: List<String>): ByteArray {
        return client.get(ids.getPdfUrl()).body()
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
        subList.chunked(150)
            .flatMap { strings ->
                strings.pmap { it to getReports(course = it).results.isNotEmpty() }
            }.mapNotNull { (key, present) ->
                key.takeIf { present }
            }.also {
                println(it)
                delay(10000L)
            }
    }
    println(validKeys.joinToString("\", \"", "\"", "\","))
    return validKeys
}