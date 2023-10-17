package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.decodeJson
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

suspend fun getInstructorReports(writeDir: Path) {
    val profs = writeDir.resolve("profs.json").decodeJson<List<TXSTInstructor>>()
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
        writeDir.resolve("reports/${prof.plid}.json").writeAsJson(reports)
    }
}

suspend fun getAndSaveBaseProfData(writeDir: Path): List<TXSTInstructor> {
    val allInstructors = getBaseProfData()
    writeDir.resolve("profs.json").writeAsJson(allInstructors)
    return allInstructors
}