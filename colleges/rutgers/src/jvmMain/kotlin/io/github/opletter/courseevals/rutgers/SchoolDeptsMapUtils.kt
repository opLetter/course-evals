package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.decodeFromString
import io.github.opletter.courseevals.common.remote.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun copyInstructorStatsWithoutStats() {
    getCompleteSchoolDeptsMap<Map<String, InstructorStats>>("jsonData/statsByProf").mapEachDept { _, _, stats ->
        stats.mapValues { (_, stats) ->
            InstructorStats(
                lastSem = stats.lastSem,
                overallStats = emptyList(),
                courseStats = stats.courseStats.mapValues { emptyList() }
            )
        }
    }.writeToFiles("jsonData/statsByProf-cleaned")
    val allInstructors = File("jsonData/statsByProf/allInstructors.json")
        .decodeFromString<Map<String, List<Instructor>>>()
    makeFileAndDir("jsonData/statsByProf-cleaned/allInstructors.json")
        .writeText(Json.encodeToString(allInstructors))
}

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: String,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    if (writeSchoolMap) {
        val dirMap = File("jsonData/entries/schools.json").decodeFromString<Map<String, School>>()
            .mapValues { (code, school) ->
                val filteredDepts = school.depts.filter { this[code]?.keys?.contains(it) == true }
                school.copy(depts = filteredDepts.toSet())
            }.filterValues { it.depts.isNotEmpty() }
        val file = makeFileAndDir("$writeDir/schools.json")
        file.writeText(Json.encodeToString(dirMap))
    }
    forEachDept { school, dept, reports ->
        makeFileAndDir("$writeDir/$school/$dept.json")
            .writeText(Json.encodeToString(reports))
    }
    return this
}