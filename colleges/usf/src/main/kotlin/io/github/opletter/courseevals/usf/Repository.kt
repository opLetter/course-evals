package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

private val client = HttpClient().config {
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000 * 5
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
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

suspend fun getTeachingDataContent(): String {
    val payload = FormDataContent(Parameters.build {
        append("term_in", "202308")
        append("open_only", "N")
        append("begin_hh", "0")
        append("begin_mi", "0")
        append("begin_ap", "a")
        append("end_hh", "0")
        append("end_mi", "0")
        append("end_ap", "a")
        append("sel_subj", "dummy")
        append("sel_day", "dummy")
        append("sel_schd", "dummy")
        append("sel_insm", "dummy")
        append("sel_camp", "dummy")
        append("sel_levl", "dummy")
        append("sel_sess", "dummy")
        append("sel_dept", "dummy")
        append("sel_instr", "dummy")
        append("sel_ptrm", "dummy")
        append("sel_attr", "dummy")
        append("sel_subj", "%")
        append("sel_crse", "")
        append("sel_title", "")
        append("sel_schd", "%")
        append("sel_insm", "%")
        append("sel_from_cred", "")
        append("sel_to_cred", "")
        append("sel_dept", "%")
        append("sel_camp", "%")
        append("sel_levl", "%")
        append("sel_ptrm", "%")
        append("sel_instr", "%")
        append("sel_attr", "%")
    })
    return client.post("https://usfonline.admin.usf.edu/pls/prod/bwckschd.p_get_crse_unsec") {
        setBody(payload)
    }.bodyAsText()
}