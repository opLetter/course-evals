package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

@Serializable
data class PdfReport(
    val term: String,
    val course: String,
    val instructor: String,
    val questions: List<QuestionStats>,
)

@Serializable
data class ReportMetadata(
    val code: String,
    val course: String,
    val instructor: String,
    val term: String,
    val area: String,
    /** Used to get the pdf url; should be length 4 */
    val ids: List<String>,
) {
    companion object {
        fun fromString(str: String): ReportMetadata = str
            .replace("<span class=\"search-highlighted\">", "")
            .replace("</span>", "")
            .run {
                ReportMetadata(
                    code = substringAfterBefore("<p class=\"sr-dataitem-info-code\">", "</p>").trim(),
                    course = substringAfterBefore("<h4>", "</h4>").trim(),
                    instructor = substringAfter("<p class=\"sr-dataitem-info-instr\">", "")
                        .substringBefore("</p>").trim(),
                    term = substringAfterBefore("<p class=\"small\" style=\"margin-bottom:15px;\">", "<").trim(),
                    area = substringAfterBefore("<br />", "</p>").trim(),
                    ids = substringAfterBefore("data-id0", "Title")
                        .split("'")
                        .filterIndexed { index, _ -> index % 2 == 1 },
                )
            }
    }
}

@Serializable
data class Report(
    /** Note that some may erroneously have "SEE MORE ON LAST PAGE" or "SECTIONS)" before the actual name **/
    val pdfInstructor: String,
    /** Formatted as "Last, First" */
    val htmlInstructor: String,
    val term: String,
    val courseCode: String,
    val courseName: String,
    val area: String = "", // missing from <=reports-10
    val questions: List<QuestionStats>,
    val ids: List<String> = emptyList(),
    val pdfUrl: String = "",
)

@Serializable
data class QuestionStats(
    val question: String,
    val results: List<Int>, // should be size 5 - num responses of each type
    val numResponses: Int,
    val numRespondents: Int,
)

fun List<QuestionStats>.cleanText(): List<String> = map { stats -> stats.question.filter { it.code != 160 } }

suspend fun FSURepository.getReportForCourse(courseKey: String, getChunkSize: (listSize: Int) -> Int): List<Report> {
    val allReports = getAllReports(course = courseKey)
    val chunkSize = getChunkSize(allReports.size)
        .also { println("Chunk size: $it") }
    return allReports.chunked(chunkSize).flatMapIndexed { index, chunk ->
        if (index % 3 == 1) {
            println("A: Delaying 0.75 minute")
            delay(45_000L)
        }
        chunk.pmap { htmlResponse ->
            val metadata = ReportMetadata.fromString(htmlResponse)
            val report = flow { emit(getPdfBytes(metadata.ids).getStatsFromPdf()) }
                .retry(3) {
                    it.printStackTrace()
                    (it is HttpRequestTimeoutException || it is ConnectTimeoutException)
                        .also { retry ->
                            if (!retry) return@also
                            println("D: retrying, delaying 15 seconds")
                            delay(15_000)
                        }
                }.catch {
                    it.printStackTrace()
                    println("D2: Failed getting report")
                }.singleOrNull()
                ?: return@pmap Report(
                    pdfInstructor = "Report-ERROR",
                    htmlInstructor = metadata.instructor,
                    term = metadata.term,
                    courseName = metadata.course,
                    courseCode = metadata.code, // may be cut off in html/"report"
                    questions = emptyList(),
                    ids = metadata.ids, // only significant for data retrieval purposes
                )

            val (reportCode, reportCourse) = report.course.split(" : ")
                .let { if (it.size == 2) it else List(2) { "Error" } }
            if (reportCourse != metadata.course) {
                println("MismatchB: $reportCourse != ${metadata.course} $metadata")
            }
            if (reportCode != metadata.code) {
                println("MismatchB: $reportCode != ${metadata.code} $metadata")
            }
//            if (report.instructor.split(" ")
//                    .takeIf { it.size == 2 }?.let { i -> "${i[1]}, ${i[0]}" } != metadata.instructor
//            ) {
//                println("MismatchA: ${report.instructor} != ${metadata.instructor} $metadata")
//            }
            if (report.term != metadata.term) {
                println("MismatchB: ${report.term} != ${metadata.term} $metadata")
            }
            Report(
                pdfInstructor = report.instructor,
                htmlInstructor = metadata.instructor,
                term = metadata.term,
                courseName = metadata.course,
                courseCode = metadata.code, // may be cut off in html/"report"
                questions = report.questions,
                ids = metadata.ids, // only significant for data retrieval purposes
            )
        }.also { reports ->
            val tempPath = "json-data/reports-8/temp/$courseKey.json"
            val tempContents = File(tempPath).takeIf { it.exists() }?.readText() ?: "[]"
            val earlierReports = Json.decodeFromString<List<Report>>(tempContents)
            makeFileAndDir(tempPath).writeText(Json.encodeToString(earlierReports + reports))
        }
    }.sortedWith(compareBy({ it.htmlInstructor }, { it.term }))
}

fun ByteArray.getStatsFromPdf(): PdfReport {
    PDDocument.load(this).use { doc ->
        val text = PDFTextStripper().getText(doc)
        val generalData = text.substringAfterBefore("Florida State University\r\n", "Instructor:").lines()
        val stats = text.split("\\d+ - ".toRegex())
            .drop(1)
            .mapNotNull { str ->
                val lines = str.substringBefore("Florida State University")
                    .trimEnd().dropLast(1).trim().lines()
                    // only take lines that are formatted as questions results - reduce false positives
                    .also { if (it.getOrNull(1)?.startsWith("Response") != true) return@mapNotNull null }

                val responseRate = lines.last().substringBefore(" ").split("/")
                QuestionStats(
                    question = lines[0], //.filter { it.code != 160 },
                    results = lines.drop(2)
                        .takeWhile { it[0] != '0' }
                        .map { it.substringAfterBefore(") ", " ").toInt() },
                    numResponses = responseRate[0].toIntOrNull() ?: (-1).also {
                        println("Invalid response rate[0]: $responseRate")
                    },
                    numRespondents = responseRate.getOrNull(1)?.toIntOrNull() ?: (-1).also {
                        println("Invalid response rate[1]: $responseRate")
                    },
                )
            }
        if (generalData.size < 3) {
            println("Invalid general data: $generalData")
            return PdfReport(
                term = "Error ${generalData.getOrNull(0)}",
                course = "Error ${generalData.getOrNull(1)}",
                instructor = "Error ${generalData.getOrNull(2)}",
                questions = stats,
            )
        }
        return PdfReport(
            term = generalData[0],
            course = generalData.drop(1).dropLast(1).joinToString("").substringAfter("Course: "),
            instructor = generalData.last(),
            questions = stats,
        )
    }
}

suspend fun getAllData(dir: String = "json-data/reports-8") {
    val repo = FSURepository().also { it.login() }

    CourseSearchKeys.forEachIndexed { index, courseKey ->
        val reports: List<Report>? = flow {
            emit(repo.getReportForCourse(courseKey) { if (it > 200) 40 else 50 }.ifEmpty { null })
        }.retry(3) {
            it.printStackTrace()
            println("B: retrying, delaying 1 minute")
            delay(60_000)
            it is HttpRequestTimeoutException || it is ConnectTimeoutException
        }.catch {
            it.printStackTrace()
            println("B2: failed 3 times. delaying 1 minute")
            delay(60_000)
        }.singleOrNull()

        reports?.let {
            makeFileAndDir("$dir/$courseKey.json").writeText(Json.encodeToString(it))
        } ?: makeFileAndDir("$dir/failed/$courseKey.json").writeText("{}")

        println("C: Done with key $courseKey, delaying 0.5 minute")
        delay(30_000)
        if (index % 11 == 10) {
            println("E: Taking a well-deserved break (2 minutes)")
            delay(120_000)
        }
    }
}