package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.decodeJson
import io.github.opletter.courseevals.common.remote.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.common.remote.writeAsJson
import io.github.opletter.courseevals.txst.remote.data.Report
import io.github.opletter.courseevals.txst.remote.data.SaveableResponse
import io.github.opletter.courseevals.txst.remote.data.TXSTInstructor
import java.io.File

fun getStatsByProf(readDir: String, writeDir: String?): Map<String, Map<String, InstructorStats>> {
    val allProfs = File("$readDir/profs.json")
        .decodeJson<List<TXSTInstructor>>()
        .associateBy { it.plid }

    // kinda ugly but it works
    val statsByProf = File("$readDir/reports").listFiles()!!
        .flatMap { it.decodeJson<List<Report<SaveableResponse>>>() }
        .groupBy { report ->
            report.course.number.takeWhile { it.isLetter() }
                .also { check(it in Prefixes) { "$it not a prefix" } }
        }.mapValues { (_, reports) ->
            reports
                .distinct() // courses w/ multiple instructors can lead to duplicate reports
                .flatMap { report ->
                    report.responses.map {
                        report.course to (it.instructor.plid to it.scores)
                    }
                }.groupBy { it.second.first }
                .entries
                .associate { (profId, profStats) ->
                    allProfs[profId]!!.displayName.normalizeName() to InstructorStats(
                        lastSem = parseSemester(profStats.maxOf { it.first.semester }),
                        overallStats = profStats.map { it.second.second }.combine(),
                        courseStats = profStats.groupBy(
                            keySelector = { (course, _) ->
                                course.number.dropWhile { it.isLetter() }
                            },
                            valueTransform = { it.second.second }
                        ).mapValues { it.value.combine() }.toSortedMap().toMap()
                    )
                }.toSortedMap().toMap()
        }

    statsByProf.forEach { (prefix, profs) ->
        makeFileAndDir("$writeDir/0/$prefix.json").writeAsJson(profs.toSortedMap().toMap())
    }
    return statsByProf
}

fun getSchoolsData(writeDir: String?): School {
    return School("0", "All", Prefixes.toSet(), setOf(Campus.MAIN), LevelOfStudy.U).also {
        if (writeDir == null) return@also
        makeFileAndDir("$writeDir/schools.json").writeAsJson(mapOf("0" to it))
    }
}

fun getAllInstructors(readDir: String, writeDir: String?): List<Instructor> {
    return getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir)
        .getValue("0")
        .flatMap { (subject, stats) ->
            stats.map { Instructor(it.key, subject, it.value.lastSem) }
        }.also {
            if (writeDir == null) return@also
            makeFileAndDir("$writeDir/instructors.json").writeAsJson(mapOf("0" to it))
        }
}

// For some reason, for fall the "year" part is  1 + the actual year
private fun parseSemester(semester: Int): Int {
    val type = when (semester % 100) {
        10 -> SemesterType.Fall
        30 -> SemesterType.Spring
        50 -> SemesterType.Summer
        else -> error("Invalid semester: $semester")
    }
    val year = semester / 100 - if (type == SemesterType.Fall) 1 else 0
    return Semester.Triple.valueOf(type, year).numValue
}

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