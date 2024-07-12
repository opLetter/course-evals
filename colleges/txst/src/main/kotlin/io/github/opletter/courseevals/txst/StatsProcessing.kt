package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.Instructor
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.combine
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.txst.remote.data.Report
import io.github.opletter.courseevals.txst.remote.data.SaveableResponse
import io.github.opletter.courseevals.txst.remote.data.TXSTInstructor
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

fun getStatsByProf(reportsDir: Path, profs: List<TXSTInstructor>): Map<String, Map<String, InstructorStats>> {
    val allProfs = profs.associateBy { it.plid }

    // kinda ugly but it works
    return reportsDir.listDirectoryEntries()
        .flatMap { it.decodeJson<List<Report<SaveableResponse>>>() }
        .groupBy { report ->
            report.course.number.takeWhile { it.isLetter() }
                .also { check(it in Prefixes) { "$it not a prefix" } }
        }.mapValues { (_, reports) ->
            reports
                .toSet() // courses w/ multiple instructors can lead to duplicate reports
                .flatMap { report ->
                    report.responses.map { Triple(report.course, it.instructor.plid, it.scores) }
                }.groupBy { it.second }
                .entries
                .associate { (profId, profStats) ->
                    allProfs.getValue(profId).displayName.normalizeName() to InstructorStats(
                        lastSem = parseSemester(profStats.maxOf { it.first.semester }).numValue,
                        overallStats = profStats.map { it.third }.combine(),
                        courseStats = profStats.groupBy(
                            keySelector = { (course, _) ->
                                course.number.dropWhile { it.isLetter() }
                            },
                            valueTransform = { it.third }
                        ).mapValues { it.value.combine() }.toSortedMap().toMap()
                    )
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

// For some reason, for fall the "year" part is  1 + the actual year

private fun String.normalizeName(): String {
    val parts = split(" ").filter { it.isNotEmpty() }
        .let { if (it.first().endsWith('.')) it.drop(1) else it } // drop "Dr." etc
    val last = parts.reversed().first { part ->
        !part.endsWith('.') && !part.all { it.isUpperCase() }
    }
    val indexOfLast = parts.indexOf(last)
    val firstParts = parts.subList(0, indexOfLast) + parts.drop(indexOfLast + 1)
    return last + ", " + firstParts.joinToString(" ")
}