package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
    // Note that some may erroneously have "SEE MORE ON LAST PAGE" or "SECTIONS)" before the actual name
    // "Report-ERROR" represents broken pdf
    val pdfInstructor: String,
    // Formatted as "Last, First"
    val htmlInstructor: String,
    // Formatted as "2020 Fall"
    val term: String,
    val courseCode: String,
    val courseName: String,
    val area: String,
    // may be -1
    val numRespondents: Int,
    // keys are question numbers from (see [QuestionMapping]), values are # of ratings 5-1 (# of 5s, # of 4s, ...)
    val ratings: Map<Int, List<Int>>,
    // used to get the pdf url
    val ids: List<String>,
) {
    companion object {
        fun from(pdfReport: PdfReport, metadata: ReportMetadata): Report {
            return Report(
                pdfInstructor = pdfReport.instructor,
                htmlInstructor = metadata.instructor,
                term = metadata.term,
                courseCode = metadata.code,
                courseName = metadata.course,
                area = metadata.area,
                numRespondents = (pdfReport.questions.map { it.numRespondents } - (-1)).distinct().singleOrNull() ?: -1,
                ratings = pdfReport.questions.associate {
                    val cleanQuestion = it.question.replace("\u00A0", "")
                    QuestionMapping[cleanQuestion]!! to it.results
                },
                ids = metadata.ids,
            )
        }
    }
}

suspend fun FSURepository.getReportsForCourse(
    courseKey: String,
    tempDir: String,
    getChunkSize: (listSize: Int) -> Int,
): List<Report> {
    val allReports = getAllReports(course = courseKey)
    val chunkSize = getChunkSize(allReports.size)
        .also { println("Chunk size: $it") }
    return allReports.chunked(chunkSize).flatMapIndexed { index, chunk ->
        if (index % 3 == 1) {
            println("A: Delaying 0.75 minute")
            delay(0.75.minutes)
        }
        chunk.pmap { htmlResponse ->
            val metadata = ReportMetadata.fromString(htmlResponse)
            val pdfReport = flow { emit(getPdfBytes(metadata.ids).getStatsFromPdf()) }
                .retry(3) {
                    it.printStackTrace()
                    (it is HttpRequestTimeoutException || it is ConnectTimeoutException)
                        .also { retry ->
                            if (!retry) return@also
                            println("D: retrying, delaying 15 seconds")
                            delay(15.seconds)
                        }
                }.catch {
                    it.printStackTrace()
                    println("D2: Failed getting report")
                }.singleOrNull()
                ?: return@pmap Report.from(
                    PdfReport("Report-ERROR", "Report-ERROR", "Report-ERROR", emptyList()),
                    metadata,
                )

            val (reportCode, reportCourse) = pdfReport.course.split(" : ")
                .let { if (it.size == 2) it else List(2) { "Error" } }
            if (reportCourse != metadata.course) {
                println("MismatchB: $reportCourse != ${metadata.course} $metadata")
            }
            if (reportCode != metadata.code) {
                println("MismatchB: $reportCode != ${metadata.code} $metadata")
            }
            if (pdfReport.term != metadata.term) {
                println("MismatchB: ${pdfReport.term} != ${metadata.term} $metadata")
            }
            Report.from(pdfReport, metadata)
        }.also { reports ->
            val tempPath = "$tempDir/$courseKey.json"
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
                    numResponses = responseRate[0].toIntOrNull() ?: -1,
                    numRespondents = responseRate.getOrNull(1)?.toIntOrNull() ?: -1,
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

suspend fun getAllData(writeDir: String, keys: List<String> = CourseSearchKeys) {
    val repo = FSURepository().also { it.login() }

    keys.forEachIndexed { index, courseKey ->
        val reports: List<Report>? = flow {
            val reports = repo.getReportsForCourse(courseKey, "$writeDir/temp") { if (it > 200) 40 else 50 }
            emit(reports.ifEmpty { null })
        }.retry(3) {
            it.printStackTrace()
            println("B: retrying, delaying 1 minute")
            delay(1.minutes)
            it is HttpRequestTimeoutException || it is ConnectTimeoutException
        }.catch {
            it.printStackTrace()
            println("B2: failed 3 times. delaying 1 minute")
            delay(1.minutes)
        }.singleOrNull()

        reports?.let {
            makeFileAndDir("$writeDir/$courseKey.json")
                .writeText(Json.encodeToString(it.distinct())) // for some reason there may be a few duplicates
        } ?: makeFileAndDir("$writeDir/failed/$courseKey.json").writeText("{}")

        println("C: Done with key $courseKey, delaying 0.5 minute")
        delay(0.5.minutes)
        if (index % 11 == 10) {
            println("E: Taking a well-deserved break (2 minutes)")
            delay(2.minutes)
        }
    }
}