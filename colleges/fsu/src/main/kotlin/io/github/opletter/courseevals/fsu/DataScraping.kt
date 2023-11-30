package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.Serializable
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.time.Duration
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
    // Used to get the pdf url; should be length 4
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
    /**
     * The instructor name parsed from the pdf report.
     *
     * Note that this may contain incorrect values, like "SEE MORE ON LAST PAGE" or "SECTIONS)" before the actual name.
     *
     * "Report-ERROR" represents a broken (inaccessible) pdf.
     */
    val pdfInstructor: String,
    /** The instructor name as it appears in the html search results. Formatted as "Last, First". */
    val htmlInstructor: String,
    /** The term as it appears in the html search results. Formatted as "2023 Spring". */
    val term: String,
    /**
     * The course code as it appears in the html search results.
     *
     * Includes section numbers, and may contain several codes combined by slashes.
     */
    val courseCode: String,
    /** The course name as it appears in the html search results. */
    val courseName: String,
    /** The area as it appears in the html search results. */
    val area: String,
    /** The number of respondents as parsed from the pdf report. May be -1. */
    val numRespondents: Int,
    /**
     * The ratings, gathered from the pdf report.
     *
     * Keys are question numbers from [QuestionMapping], values are # of ratings 5-1 (# of 5s, # of 4s, ...).
     */
    val ratings: Map<Int, List<Int>>,
    /**
     * The ids present in the html search results, used for getting the pdf report url. Should have a length of 4.
     */
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
                ratings = pdfReport.questions.associate { QuestionMapping.getValue(it.question) to it.results },
                ids = metadata.ids,
            )
        }
    }
}

suspend fun FSURepository.getReportsForCourse(
    courseKey: String,
    tempDir: Path,
    getChunkSize: (listSize: Int) -> Int,
): List<Report> {
    val allReports = getAllReports(course = courseKey)
    val chunkSize = getChunkSize(allReports.size)
        .also { println("Chunk size: $it") }
    return allReports.chunked(chunkSize).flatMapIndexed { index, chunk ->
        if (index % 3 == 1) {
            delayAndLog(0.75.minutes) { "A: Delaying $it" }
        }
        chunk.pmap { htmlResponse ->
            val metadata = ReportMetadata.fromString(htmlResponse)
            val pdfReport = flow { emit(getPdfBytes(metadata.ids).getStatsFromPdf()) }
                .retry(3) {
                    it.printStackTrace()
                    if (it is HttpRequestTimeoutException || it is ConnectTimeoutException) {
                        delayAndLog(15.seconds) { time -> "D: retrying, delaying $time" }
                        true
                    } else false
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
            val tempPath = tempDir / "${courseKey.take(3)}/${courseKey.drop(3)}.json"
            val earlierReports = tempPath.decodeJsonIfExists<List<Report>>().orEmpty()
            tempPath.writeAsJson(earlierReports + reports)
        }
    }.sortedWith(compareBy({ it.htmlInstructor }, { it.term }))
}

fun ByteArray.getStatsFromPdf(): PdfReport {
    Loader.loadPDF(this).use { doc ->
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
                    question = lines[0].replace("\u00A0", ""),
                    results = lines.drop(2)
                        .takeWhile { it[0] != '0' }
                        .map { it.substringAfterBefore(") ", " ").toInt() },
                    numResponses = responseRate[0].toIntOrNull() ?: -1,
                    numRespondents = responseRate.getOrNull(1)?.toIntOrNull() ?: -1,
                )
            }
        return if (generalData.size < 3) {
            println("Invalid general data: $generalData")
            PdfReport(
                term = "Error ${generalData.getOrNull(0)}",
                course = "Error ${generalData.getOrNull(1)}",
                instructor = "Error ${generalData.getOrNull(2)}",
                questions = stats,
            )
        } else {
            PdfReport(
                term = generalData[0],
                course = generalData.drop(1).dropLast(1).joinToString("").substringAfter("Course: "),
                instructor = generalData.last(),
                questions = stats,
            )
        }
    }
}

suspend fun getAllData(outputDir: Path, keys: List<String> = CourseSearchKeys) {
    val repo = FSURepository.initLoggedIn()

    keys.forEachIndexed { index, courseKey ->
        val reports: List<Report>? = flow {
            val reports = repo.getReportsForCourse(courseKey, outputDir / "temp") { if (it > 200) 40 else 50 }
            emit(reports.ifEmpty { null })
        }.retry(3) {
            it.printStackTrace()
            delayAndLog(1.minutes) { time -> "B: retrying, delaying $time" }
            it is HttpRequestTimeoutException || it is ConnectTimeoutException
        }.catch {
            it.printStackTrace()
            delayAndLog(1.minutes) { time -> "B2: failed 3 times. delaying $time" }
        }.singleOrNull()

        if (reports != null) {
            outputDir.resolve("${courseKey.take(3)}/${courseKey.drop(3)}.json")
                .writeAsJson(reports.distinct()) // for some reason there may be a few duplicates
        } else {
            outputDir.resolve("failed/${courseKey.take(3)}/${courseKey.drop(3)}.json")
                .createParentDirectories().writeText("{}")
        }

        delayAndLog(0.5.minutes) { time -> "C: Done with key $courseKey, delaying $time" }
        if (index % 11 == 10) {
            delayAndLog(2.minutes) { time -> "E: Taking a well-deserved break ($time)" }
        }
    }
}

private suspend fun delayAndLog(time: Duration, message: (time: Duration) -> String) {
    println(message(time))
    delay(time)
}