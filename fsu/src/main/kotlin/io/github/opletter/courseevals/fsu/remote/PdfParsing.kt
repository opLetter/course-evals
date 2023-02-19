package io.github.opletter.courseevals.fsu.remote

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.PdfReport
import io.github.opletter.courseevals.fsu.QuestionStats
import io.github.opletter.courseevals.fsu.Report
import io.github.opletter.courseevals.fsu.ReportMetadata
import kotlinx.coroutines.delay
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

suspend fun FSURepository.getReportForCourse(courseKey: String, getChunkSize: (listSize: Int) -> Int): List<Report> {
    val allReports = getAllReportsAsync(course = courseKey)
    val chunkSize = getChunkSize(allReports.size)
        .also { println("Chunk size: $it") }
    return allReports.chunked(chunkSize).flatMapIndexed { index, chunk ->
        if (index % 2 == 1) {
            println("A: Delaying 1 minute")
            delay(60_000L)
        }
        chunk.pmap { htmlResponse ->
            val metadata = ReportMetadata.fromString(htmlResponse)
            val ids = htmlResponse.substringAfterBefore("data-id0", "Title")
                .split("'")
                .filterIndexed { index, _ -> index % 2 == 1 }
            val report = getPdfBytes(ids).getStatsFromPdf()
            val (reportCode, reportCourse) = report.course.split(" : ")
                .let { if (it.size == 2) it else List(2) { "Error" } }
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
                courseCode = metadata.code, // may be cut off in html/"report"
                questions = report.questions,
                ids = ids, // only significant for data retrieval purposes
            )
        }
    }.sortedWith(compareBy({ it.htmlInstructor }, { it.term }))
}

fun ByteArray.getStatsFromPdf(): PdfReport {
    PDDocument.load(this).use { doc ->
        val text = PDFTextStripper().getText(doc)
        val generalData = text.substringAfterBefore("Florida State University\r\n", "Instructor:").lines()
        val stats = text.split("\\d - ".toRegex())
            .drop(1)
            .map { str ->
                val lines = str.substringBefore("Florida State University")
                    .trimEnd().dropLast(1).trim().lines()
                val responseRate = lines.last().substringBefore(" ").split("/")
                QuestionStats(
                    question = lines[0],
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
            course = generalData[1].substringAfter("Course: "),
            instructor = generalData[2],
            questions = stats,
        )
    }
}