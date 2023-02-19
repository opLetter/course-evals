package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.github.opletter.courseevals.fsu.remote.getReportForCourse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun main() {
    val repo = FSURepository()
    println(repo.login())

    CourseSearchKeys.forEach { courseKey ->
        val reports: List<Report>? = flow {
            emit(repo.getReportForCourse(courseKey) { if (it > 200) 25 else if (it > 100) 30 else 40 })
        }.retry(3) {
            it.printStackTrace()
            println("B: retrying, delaying 1 minute")
            delay(10_000)
            true
        }.catch {
            it.printStackTrace()
            println("B2: failed 3 times. delaying 1 minute")
            delay(60_000)
        }.singleOrNull()

        reports?.let {
            makeFileAndDir("json-data/reports-7/$courseKey.json").writeText(Json.encodeToString(it))
        } ?: makeFileAndDir("json-data/reports-7/failed/$courseKey.json").writeText("{}")

        println("C: Done with key $courseKey, delaying 1 minute")
        delay(60_000)
    }
}

fun makeFileAndDir(filename: String): File = File(filename).apply { parentFile.mkdirs() }

@Serializable
data class ReportMetadata(
    val code: String,
    val course: String,
    val instructor: String,
    val term: String,
    val area: String,
) {
    companion object {
        fun fromString(str: String): ReportMetadata {
            return str
                .replace("<span class=\"search-highlighted\">", "")
                .replace("</span>", "")
                .run {
                    ReportMetadata(
                        substringAfterBefore("<p class=\"sr-dataitem-info-code\">", "</p>").trim(),
                        substringAfterBefore("<h4>", "</h4>").trim(),
                        substringAfterBefore("<p class=\"sr-dataitem-info-instr\">", "</p>").trim(),
                        substringAfterBefore("<p class=\"small\" style=\"margin-bottom:15px;\">", "<").trim(),
                        substringAfterBefore("<br />", "</p>").trim(),
                    )
                }
        }
    }
}

@Serializable
data class Report(
    /** Formatted as "Last, First" */
    val pdfInstructor: String,
    val htmlInstructor: String,
    val term: String,
    val courseCode: String,
    val courseName: String,
    val questions: List<QuestionStats>,
    val ids: List<String> = emptyList(),
)

@Serializable
data class PdfReport(
    val term: String,
    val course: String,
    val instructor: String,
    val questions: List<QuestionStats>,
)

@Serializable
data class QuestionStats(
    val question: String,
    val results: List<Int>, // should be size 5 - num responses of each type
    val numResponses: Int,
    val numRespondents: Int,
)
