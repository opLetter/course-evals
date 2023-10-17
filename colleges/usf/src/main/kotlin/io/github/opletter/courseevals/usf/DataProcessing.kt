package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.writeAsJson
import java.nio.file.Path

fun List<Report>.getTotalRatings(): Ratings {
    return map { it.ratings }.combine()
}

fun getTerms(): List<String> {
    return listOf("01", "05", "08").flatMap { sem ->
        (2006..2022).map { year -> "$year$sem" }
    } + setOf("200508", "202301")
}

fun getReportsFromFiles(dir: Path): Map<String, List<Report>> {
    val terms = getTerms()
    return Prefixes.associateWith { prefix ->
        terms.flatMap { dir.resolve(prefix).resolve("$it.json").decodeJson<List<Report>>() }
    }
}

fun getStatsByProf(
    data: Map<String, List<Report>>,
    writeDir: Path?,
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
    }.onEach { (prefix, profs) ->
        if (profs.isEmpty()) {
            println("no profs for $prefix")
            return@onEach
        }
        writeDir?.resolve("0/$prefix.json")?.writeAsJson(profs) // "0" is the school
    }
}

fun getAllInstructors(readDir: Path, writeDir: Path?): List<Instructor> {
    return getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir)
        .getValue("0")
        .flatMap { (subject, stats) ->
            stats.map { Instructor(it.key, subject, it.value.lastSem) }
        }.also { writeDir?.resolve("instructors.json")?.writeAsJson(mapOf("0" to it)) }
}

fun getSchoolsData(writeDir: Path?): School {
    return School("0", "All", Prefixes.toSet(), setOf(Campus.MAIN), LevelOfStudy.U)
        .also { writeDir?.resolve("schools.json")?.writeAsJson(mapOf("0" to it)) }
}