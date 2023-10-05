package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.decodeFromString
import io.github.opletter.courseevals.common.remote.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun List<Report>.getTotalRatings(): Ratings {
    return map { it.ratings }.combine()
}

fun getTerms(): List<String> {
    return listOf("01", "05", "08").flatMap { sem ->
        (2006..2022).map { year -> "$year$sem" }
    } + setOf("200508", "202301")
}

fun getReportsFromFiles(dir: String): Map<String, List<Report>> {
    val terms = getTerms()
    return Prefixes.associateWith { prefix ->
        terms.flatMap { File("$dir/$prefix/$it.json").decodeFromString<List<Report>>() }
    }
}

fun getStatsByProf(
    data: Map<String, List<Report>>,
    writeDir: String?,
    minSem: Semester.Triple = Semester.Triple.valueOf(SemesterType.Fall, 2012),
): Map<String, Map<String, InstructorStats>> {
    return data.mapValues { (_, entries) ->
        entries
            .filter { !it.deptInfo.startsWith("Lakeland") && Semester.Triple.valueOf(it.term) >= minSem }
            .groupBy { it.prof.uppercase().replace(" ", "") }
            .map { (_, profEntries) ->
                val newKey = profEntries.maxBy { Semester.Triple.valueOf(it.term).numValue }.prof
                newKey to InstructorStats(
                    lastSem = profEntries.maxOf { Semester.Triple.valueOf(it.term).numValue },
                    overallStats = profEntries.getTotalRatings(),
                    courseStats = profEntries.groupBy {
                        it.courseID.substringAfterBefore(" - ", " ")
                    }.mapValues { (_, reports) -> reports.getTotalRatings() }
                )
            }.toMap()
    }.also {
        if (writeDir == null) return@also
        it.forEach { (prefix, profs) ->
            if (profs.isEmpty()) {
                println("no profs for $prefix")
                return@forEach
            }
            makeFileAndDir("$writeDir/0/$prefix.json") // "0" is the school
                .writeText(Json.encodeToString(profs))
        }
    }
}

fun getAllInstructors(readDir: String, writeDir: String?): List<Instructor> {
    return getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir)
        .getValue("0")
        .flatMap { (subject, stats) ->
            stats.map { Instructor(it.key, subject, it.value.lastSem) }
        }.also {
            if (writeDir == null) return@also
            makeFileAndDir("$writeDir/instructors.json")
                .writeText(Json.encodeToString(mapOf("0" to it)))
        }
}

fun getSchoolsData(writeDir: String?): School {
    return School("0", "All", Prefixes.toSet(), setOf(Campus.MAIN), LevelOfStudy.U).also {
        if (writeDir == null) return@also
        makeFileAndDir("$writeDir/schools.json")
            .writeText(Json.encodeToString(mapOf("0" to it)))
    }
}