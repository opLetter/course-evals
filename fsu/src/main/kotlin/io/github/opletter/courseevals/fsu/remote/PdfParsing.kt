package io.github.opletter.courseevals.fsu.remote

import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.fsu.PdfReport
import io.github.opletter.courseevals.fsu.QuestionStats
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

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
                    lines[0],
                    lines.drop(2)
                        .takeWhile { it[0] != '0' }
                        .map { it.substringAfterBefore(") ", " ").toInt() },
                    responseRate[0].toInt(),
                    responseRate[1].toInt(),
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