package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.fsu.remote.FSURepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import java.nio.file.Path
import kotlin.time.Duration.Companion.seconds

fun validateReports(oldDir: Path, newDir: Path) {
    CourseSearchKeys.forEach { prefix ->
        val reports = newDir.resolve("$prefix.json").decodeJson<List<Report>>()

        val oldReports = oldDir.resolve("$prefix.json").decodeJsonIfExists<List<Report>>().orEmpty()

        println("oldReports: ${oldReports.size}, reports: ${reports.size}")
        val missingReports = oldReports.filter { oldReport ->
            oldReport.pdfInstructor != ReportError && reports.singleOrNull {
                oldReport.term == it.term && oldReport.courseCode == it.courseCode
                        && (oldReport.pdfInstructor == it.pdfInstructor || it.htmlInstructor == oldReport.htmlInstructor)
                        && oldReport.ratings.values.firstOrNull() == it.ratings.values.firstOrNull()
            } == null
        }.onEach { println(it) }

        val newErrorReports = reports.filter { it.pdfInstructor == ReportError }

        println("missingReports: ${missingReports.size}, newErrorReports: ${newErrorReports.size}")
    }
}

suspend fun fixReportErrors(oldDir: Path, newDir: Path) {
    val repository = FSURepository.initLoggedIn()
    CourseSearchKeys.forEach { prefix ->
        println("starting $prefix")

        val reports = oldDir.resolve("${prefix.take(3)}/${prefix.drop(3)}.json").decodeJson<List<Report>>()

        val improvedReports = reports.pmap { report ->
            if (report.pdfInstructor != ReportError) return@pmap report

            val newReport = flow { emit(repository.getPdfBytes(report.ids).getStatsFromPdf()) }
                .retry(3) {
                    if (it.message?.contains("End-of-File") == true) {
                        println("EOF, giving up")
                        false
                    } else {
                        println("retrying, delaying 30 seconds")
                        delay(30.seconds)
                        true
                    }
                }.catch {
                    println("Z: Exception\n${it.message}")
                }.singleOrNull()

            newReport?.let {
                Report.from(
                    pdfReport = it,
                    metadata = ReportMetadata(
                        code = report.courseCode,
                        course = report.courseName,
                        instructor = report.htmlInstructor,
                        term = report.term,
                        area = report.area,
                        ids = report.ids,
                    ),
                )
            } ?: report
        }

        newDir.resolve("${prefix.take(3)}/${prefix.drop(3)}.json").writeAsJson(improvedReports)

        println("finished $prefix")
    }
}