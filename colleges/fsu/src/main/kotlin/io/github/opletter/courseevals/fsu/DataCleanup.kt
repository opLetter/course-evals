package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.makeFileAndDir
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.fsu.remote.FSURepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.singleOrNull
import java.io.File
import kotlin.time.Duration.Companion.seconds

fun validateReports(oldDir: String, newDir: String) {
    CourseSearchKeys.forEach { prefix ->
        val reports = File("$newDir/$prefix.json").decodeJson<List<Report>>()

        val oldReports = File("$oldDir/$prefix.json")
            .takeIf { it.exists() }
            ?.decodeJson<List<Report>>()
            .orEmpty()

        println("oldReports: ${oldReports.size}, reports: ${reports.size}")
        val missingReports = oldReports.filter { oldReport ->
            oldReport.pdfInstructor != "Report-ERROR" && reports.singleOrNull {
                oldReport.term == it.term && oldReport.courseCode == it.courseCode
                        && (oldReport.pdfInstructor == it.pdfInstructor || it.htmlInstructor == oldReport.htmlInstructor)
                        && oldReport.ratings.values.firstOrNull() == it.ratings.values.firstOrNull()
            } == null
        }.onEach { println(it) }

        val newErrorReports = reports.filter { it.pdfInstructor == "Report-ERROR" }

        println("missingReports: ${missingReports.size}, newErrorReports: ${newErrorReports.size}")
    }
}

suspend fun fixReportErrors(oldDir: String, newDir: String) {
    val repository = FSURepository.initLoggedIn()
    CourseSearchKeys.forEach { prefix ->
        println("starting $prefix")

        val reports = File("$oldDir/$prefix.json").decodeJson<List<Report>>()

        val improvedReports = reports.pmap { report ->
            if (report.pdfInstructor != "Report-ERROR") return@pmap report

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

        makeFileAndDir("$newDir/$prefix.json").writeAsJson(improvedReports)

        println("finished $prefix")
    }
}