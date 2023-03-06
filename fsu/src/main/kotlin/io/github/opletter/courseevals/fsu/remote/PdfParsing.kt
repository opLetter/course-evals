package io.github.opletter.courseevals.fsu.remote

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

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