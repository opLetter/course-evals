package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import java.nio.file.Path

fun List<Report>.getTotalRatings(): Ratings {
    return map { it.ratings }.combine()
}

fun getTerms(): List<String> {
    return listOf("01", "05", "08").flatMap { sem ->
        (2006..2024).map { year -> "$year$sem" }
    } + setOf("200508")
}

fun getReportsFromFiles(dir: Path): Map<String, List<Report>> {
    val terms = getTerms()
    return Prefixes.associateWith { prefix ->
        terms.flatMap { dir.resolve(prefix).resolve("$it.json").decodeJson<List<Report>>() }
    }
}

fun getStatsByProf(
    data: Map<String, List<Report>>,
    minSem: Semester.Triple = Semester.Triple.valueOf(SemesterType.Fall, 2013),
): Map<String, Map<String, InstructorStats>> {
    return data.mapValues { (_, entries) ->
        entries
            .filter { !it.deptInfo.startsWith("Lakeland") && Semester.Triple.valueOf(it.term) >= minSem }
            .groupBy { it.prof.uppercase().replace(" ", "") }
            .entries
            .associate { (_, profEntries) ->
                val newKey = profEntries.maxBy { Semester.Triple.valueOf(it.term).numValue }.prof
                newKey to InstructorStats(
                    lastSem = profEntries.maxOf { Semester.Triple.valueOf(it.term).numValue },
                    overallStats = profEntries.getTotalRatings(),
                    courseStats = profEntries.groupBy {
                        it.courseID.substringAfterBefore(" - ", " ")
                    }.mapValues { (_, reports) -> reports.getTotalRatings() }
                )
            }
    }.onEach { (prefix, profs) ->
        if (profs.isEmpty()) {
            println("no profs for $prefix")
            return@onEach
        }
    }
}

fun getAllInstructors(statsByProfDir: Path): List<Instructor> {
    return getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(statsByProfDir)
        .getValue("0")
        .flatMap { (subject, stats) ->
            stats.map { Instructor(it.key, subject, it.value.lastSem) }
        }
}