package io.github.opletter.courseevals.fsu.remote

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import kotlin.time.Duration.Companion.seconds

class FSURepository(private val cookie: String) {
    private val client = HttpClient(CIO) {
        engine {
//            proxy = ProxyBuilder.http("https://35.185.196.38:3128")
            endpoint {
                connectAttempts = 5
            }
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            requestTimeoutMillis = 30000 // 180000
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
            url {
                protocol = URLProtocol.HTTPS
                host = "fsu.evaluationkit.com"
            }
            header("Cookie", cookie)
        }
    }

    suspend fun login(): String {
        val param = "3DmbuNmXaPhUXKr4d7FOtAo8PcT3dHnl6vSswkzeYZ43uZke1%2bRiq%2bHDlbg07k3A"
        return client.get("Login/ReportPublic?id=$param").body()
    }

    suspend fun getReports(
        course: String = "",
        instructor: String = "",
        termId: String = "",
        search: Boolean = true,
        page: Int = 1,
        questionKey: String = "108347-0", // faster with this default, but also results in duplicate data across pages
        areaId: String = "",
        sort: FSUReportSort = FSUReportSort.BEST_MATCH,
    ): FSUResponse {
        require(course.length > 1 || instructor.length > 1) { "course or instructor must be at least 2 characters" }
        return client.get("AppApi/Report/PublicReport") {
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

    // up to 2024 Fall but missing 2024 Summer which doesn't exist for some reason
    private val terms = listOf(
        // no longer in list - need to confirm if data is still available
        // "2554", "2577", "2800", "2802"
        "2856", "2803", "2801", "2799", "2805", "2829", "3045", "3052", "3520", "3793", "4148", "4410", "4780", "5131",
        "5694", "5884", "6647", "7135", "7418", "7433", "7453", "7466", "7477", "7492", "7503", "7530", "7534", "7557"
    )

    suspend fun getAllReports(
        course: String = "",
        instructor: String = "",
        search: Boolean = true,
        questionKey: String = "", // should be 108347-0, but that results in duplicate data across pages
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
        }.flatten()
    }

    private suspend fun getPdfUrl(ids: List<String>): String {
        require(ids.size == 4) { "ids must be a list of 4 strings" }
        return client.get("Reports/SRPdf.aspx?${ids.joinToString(",")}")
            .bodyAsText().substringAfterBefore("'../", "'")
    }

    suspend fun getPdfBytes(ids: List<String>): ByteArray {
        return client.get(getPdfUrl(ids)).body()
    }

    suspend fun getPdfUrlAndBytes(ids: List<String>): Pair<String, ByteArray> {
        val url = getPdfUrl(ids)
        return url to client.get(url).body()
    }

    companion object {
        suspend fun init(): FSURepository {
            val cookie = HttpClient { followRedirects = false }
                .get("https://fsu.evaluationkit.com/Report/Public")
                .setCookie()
                .let {
                    val cookieKey = "ASP.NET_SessionId"
                    cookieKey + "=" + (it[cookieKey]?.value ?: error("Cookie not found"))
                }
            return FSURepository(cookie)
        }

        suspend fun initLoggedIn(): FSURepository = init().also { it.login() }
    }
}

suspend fun FSURepository.getAllValidCourseKeys(): List<String> {
    val alphabet = ('a'..'z')
    val searchKeys = alphabet.flatMap { first ->
        alphabet.flatMap { second ->
            alphabet.map { third -> "$first$second$third" }
        }
    }.flatMap { word ->
        (1..8).map { "$word$it" } // note: no existence of 0 or 9 as of 08/23, but that could change
    }

    val validKeys = searchKeys.chunked(10000).flatMap { subList ->
        subList.chunked(150)
            .flatMap { strings ->
                flow {
                    emit(strings.pmap { it to getReports(course = it).results.isNotEmpty() })
                }.retry(5) {
                    println("Retrying...")
                    delay(10.seconds)
                    true
                }.single()
            }.mapNotNull { (key, present) ->
                key.takeIf { present }
            }.also {
                println(it)
                delay(10.seconds)
            }
    }
    println(validKeys.joinToString("\", \"", "\"", "\","))
    return validKeys
}