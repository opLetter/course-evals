package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun List<FullEntry>.getTotalRatings(): Ratings {
    return map { it.ratings }.combine()
}

fun getTerms(): List<String> {
    return listOf("01", "05", "08").flatMap { sem ->
        (2005..2022).map { year -> "$year$sem" }
    } - setOf("200501", "200505")
}

fun getFullDataFromFiles(dir: String = "rawData-full"): Map<String, List<FullEntry>> {
    val terms = getTerms()
    return prefixes.associateWith { prefix ->
        terms.flatMap { term ->
            File("$dir/$prefix/$term.json").readText()
                .let { Json.decodeFromString<List<FullEntry>>(it) }
        }
    }
}

fun getStatsByProf(writeDir: String? = "jsonData/statsByProf/0"): Map<String, Map<String, InstructorStats>> {
    return getFullDataFromFiles().mapValues { (_, entries) ->
        entries.filterNot { entry ->
            entry.deptInfo.startsWith("Lakeland") ||
                    Semester.Triple.valueOf(entry.term) < Semester.Triple.valueOf(SemesterType.Fall, 2012)
        }
    }.mapValues { (_, entries) ->
        entries
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
            makeFileAndDir("$writeDir/$prefix.json")
                .writeText(Json.encodeToString(profs))
        }
    }
}

fun getAllInstructors(writeDir: String? = "jsonData/statsByProf"): List<Instructor> {
    return prefixes.associateWith { subject ->
        File("jsonData/statsByProf/0/$subject.json").readText()
            .let { Json.decodeFromString<Map<String, InstructorStats>>(it) }
    }.flatMap { (subject, stats) ->
        stats.map { Instructor(it.key, subject, it.value.lastSem) }
    }.also {
        if (writeDir == null) return@also
        makeFileAndDir("$writeDir/allInstructors.json")
            .writeText(Json.encodeToString(mapOf("0" to it)))
    }
}

fun getSchoolsData(writeDir: String? = "jsonData/statsByProf"): School {
    return School(
        "0",
        "All",
        prefixes.toSet(),
        setOf(Campus.MAIN),
        LevelOfStudy.U
    ).also {
        if (writeDir == null) return@also
        makeFileAndDir("$writeDir/schools.json")
            .writeText(Json.encodeToString(mapOf("0" to it)))
    }
}

