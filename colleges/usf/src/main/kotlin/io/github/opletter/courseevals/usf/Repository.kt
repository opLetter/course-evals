package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.*

private val client = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000 * 5
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
    install(HttpCookies)
    install(ContentNegotiation) {
        json()
    }
    BrowserUserAgent()
}

// Note: `VIEWSTATE` and EVENTVALIDATION are required, but not shared publicly as a precaution.
// If you want to run this code, you'll need to get your own VIEWSTATE and EVENTVALIDATION
// from the network tab of the evaluation website
private const val VIEWSTATE = ""
private const val EVENTVALIDATION = ""

suspend fun getReportIdByPrefix(prefix: String, term: String): String {
    val response = client.post("https://fair.usf.edu/EvaluationMart/Default.aspx") {
        val payload = FormDataContent(Parameters.build {
            append("__VIEWSTATE", VIEWSTATE)
            append("__EVENTVALIDATION", EVENTVALIDATION)
            append("ctl00\$ContentPlaceHolder1\$ctl04\$ddlTerm1", term)
            append("ctl00\$ContentPlaceHolder1\$ctl06\$ddlTerm2", term)
            append("ctl00\$ContentPlaceHolder1\$ctl08\$ddlTerm3", term)
            append("ctl00\$ContentPlaceHolder1\$ctl04\$txtCrspre", prefix)
            append("ctl00\$ContentPlaceHolder1\$ctl08\$ddlevaltype", "D")
            append("ctl00\$ContentPlaceHolder1\$ctl04\$btnCourseSubmit", "Search")
        })
        setBody(payload)
    }.bodyAsText()
    return response.substringAfterBefore("reportid=", "&")
}

private fun parseRatingsFromReport(report: String): List<List<Int>> {
    return report.substringAfter("</th></tr><tr><td align=center>")
        .split("</td><td align=center>", "</td></tr><tr><td align=center>")
        .also { check(it.size == 120) }
        .chunked(15)
        .mapIndexed { index, line ->
            val questionCode = line[0]
            check(questionCode == "E${index + 1}")
            line.drop(2).dropLast(3).filterIndexed { i, _ -> i % 2 == 0 }.reversed().map { it.toInt() }
        }
}

suspend fun getReports(reportId: String): List<Report> {
    return client.get("https://fair.usf.edu/EvaluationMart/EvaluationsReport.aspx") {
        parameter("reportid", reportId)
        parameter("reporttype", "D")
    }.bodyAsText()
        .substringBefore("<br /><br />")
        .split("</div><br><br><table width=100% class=arpt border=1><tr><th colspan=2>")
        .drop(1)
        .map { report ->
            Report(
                deptInfo = report.substringBefore("</th><th colspan=1>"),
                prof = report.substringAfterBefore("</th><th colspan=1>Instructor : ", "</th>"),
                term = report.substringAfterBefore("<th colspan=2>Course Term : ", "</th>"),
                courseTitle = report.substringAfterBefore("<th colspan=2>Course Title : ", "</th>"),
                courseID = report.substringAfterBefore("<th colspan=2>Course ID : ", "</th>"),
                enrolled = report.substringAfterBefore("<th>Number Enrolled : ", "</th>"),
                responded = report.substringAfterBefore("<th>Number Responded : ", "</th>"),
                ratings = parseRatingsFromReport(report),
            )
        }
}

suspend fun getCourseData(): List<CourseData> {
    val json = Json { ignoreUnknownKeys = true }
    // link comes from https://cloud.usf.edu/academic-programs/course-inventory
    return client.post("https://cloud.usf.edu/academic-programs/readdata/course/4")
        .bodyAsText()
        .let { json.decodeFromString<List<CourseData>>(it) }
}

class TeachingData(
    val subject: String,
    val course: String,
    val prof: String,
)

suspend fun getTeachingData(term: String): List<TeachingData> {
    val baseUrl = "https://studentssb9.it.usf.edu/StudentRegistrationSsb/ssb"
    val maxSize = 500 // API limit

    client.post("$baseUrl/term/search") {
        parameter("mode", "search")
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(parametersOf("term", term).formUrlEncode())
    }

    fun JsonElement.parseTeachingData(): TeachingData? {
        val prof = jsonObject["faculty"]!!.jsonArray.singleOrNull {
            it.jsonObject["primaryIndicator"]!!.jsonPrimitive.boolean
        } ?: return null
        return TeachingData(
            subject = jsonObject["subject"]!!.jsonPrimitive.content,
            course = jsonObject["courseNumber"]!!.jsonPrimitive.content,
            prof = prof.jsonObject["displayName"]!!.jsonPrimitive.content,
        )
    }

    return flow {
        var offset = 0
        while (true) {
            val res = client.get("$baseUrl/searchResults/searchResults") {
                parameter("txt_term", term)
                parameter("startDatepicker", "")
                parameter("endDatepicker", "")
                parameter("pageOffset", offset)
                parameter("pageMaxSize", maxSize)
                parameter("sortColumn", "subjectDescription")
                parameter("sortDirection", "asc")
            }.body<JsonObject>()

            emitAll(res["data"]!!.jsonArray.mapNotNull { it.parseTeachingData() }.asFlow())
            offset += maxSize
            if (offset >= res["totalCount"]!!.jsonPrimitive.int) break
        }
    }.toList()
}