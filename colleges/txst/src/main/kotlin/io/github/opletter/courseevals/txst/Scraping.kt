package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.remote.decodeFromString
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.txst.remote.data.Report
import io.github.opletter.courseevals.txst.remote.data.SaveableResponse
import io.github.opletter.courseevals.txst.remote.data.TXSTInstructor
import io.github.opletter.courseevals.txst.remote.data.toList
import io.github.opletter.courseevals.txst.remote.getBaseProfData
import io.github.opletter.courseevals.txst.remote.getClassesForInstructor
import io.github.opletter.courseevals.txst.remote.getInstructorDetails
import io.github.opletter.courseevals.txst.remote.getRatings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun getInstructorReports(writeDir: String) {
    val profs = File("$writeDir/profs.json").decodeFromString<List<TXSTInstructor>>()
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
        makeFileAndDir("$writeDir/reports/${prof.plid}.json")
            .writeText(Json.encodeToString(reports))
    }
}

suspend fun getAndSaveBaseProfData(writeDir: String): List<TXSTInstructor> {
    val allInstructors = getBaseProfData()
    makeFileAndDir("$writeDir/profs.json").writeText(Json.encodeToString(allInstructors))
    return allInstructors
}