package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.txst.remote.data.Report
import io.github.opletter.courseevals.txst.remote.data.SaveableResponse
import io.github.opletter.courseevals.txst.remote.data.TXSTInstructor
import io.github.opletter.courseevals.txst.remote.data.toList
import io.github.opletter.courseevals.txst.remote.getBaseProfData
import io.github.opletter.courseevals.txst.remote.getClassesForInstructor
import io.github.opletter.courseevals.txst.remote.getInstructorDetails
import io.github.opletter.courseevals.txst.remote.getRatings
import java.nio.file.Path

suspend fun getInstructorReports(outputDir: Path, profs: List<TXSTInstructor>) {
    profs.forEach { prof ->
        val details = getInstructorDetails(prof.plid)
        val reports = details.semesters.pmap { semester ->
            getClassesForInstructor(prof.plid, semester)
                .classes
                .filter { it.spi }
                .pmap { course -> getRatings(course.indexNum) }
        }.flatten().map { report ->
            Report(
                report.course,
                report.responses.map { SaveableResponse(it.responseCount, it.instructor, it.scores.toList()) }
            )
        }
        outputDir.resolve("${prof.plid}.json").writeAsJson(reports)
    }
}

suspend fun getAndSaveBaseProfData(outputFile: Path): List<TXSTInstructor> {
    val allInstructors = getBaseProfData()
    outputFile.writeAsJson(allInstructors)
    return allInstructors
}