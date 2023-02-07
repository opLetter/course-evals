package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.github.opletter.courseevals.fsu.remote.getStatsFromPdf
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun main() {
    val repo = FSURepository()
    println(repo.login())

    CourseSearchKeys.forEach { courseKey ->
        val reports = catchAndRetry(1000) { getData(repo, courseKey) }
        makeFileAndDir("json-data/reports-8/$courseKey.json")
            .writeText(Json.encodeToString(reports))
        delay(30000)
    }
}

suspend fun <T> catchAndRetry(retryLimit: Int = 5, action: suspend () -> T): T {
    if (retryLimit == 0) throw RuntimeException("Retry limit reached")
    return runCatching {
        action()
    }.getOrElse {
        println("retrying $retryLimit")
        catchAndRetry(retryLimit - 1, action)
    }
}

suspend fun getData(repo: FSURepository, courseKey: String): List<Report> {
    return repo.getAllReportsAsync(course = courseKey).chunked(50).flatMap { chunk ->
        chunk.pmap { htmlResponse ->
            val metadata = ReportMetadata.fromString(htmlResponse)
            val ids = htmlResponse.substringAfterBefore("data-id0", "Title")
                .split("'")
                .filterIndexed { index, _ -> index % 2 == 1 }
            val report = repo.getPdfBytes(ids).getStatsFromPdf()
            val (reportCode, reportCourse) = report.course.split(" : ")
            if (reportCourse != metadata.course) {
                println("MismatchB: $reportCourse != ${metadata.course} $metadata")
            }
            if (reportCode != metadata.code) {
                println("MismatchB: $reportCode != ${metadata.code} $metadata")
            }
            if (report.instructor.split(" ")
                    .takeIf { it.size == 2 }?.let { i -> "${i[1]}, ${i[0]}" } != metadata.instructor
            ) {
                println("MismatchA: ${report.instructor} != ${metadata.instructor} $metadata")
            }
            if (report.term != metadata.term) {
                println("MismatchB: ${report.term} != ${metadata.term} $metadata")
            }
            Report(
                pdfInstructor = report.instructor,
                htmlInstructor = metadata.instructor,
                term = metadata.term,
                courseName = metadata.course,
                courseCode = metadata.code, // may be cut off in html
                questions = report.questions,
            )
        }
    }.sortedWith(compareBy({ it.htmlInstructor }, { it.term }))
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
