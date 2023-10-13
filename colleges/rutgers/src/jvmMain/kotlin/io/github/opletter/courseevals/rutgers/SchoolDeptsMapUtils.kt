package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.makeFileAndDir
import io.github.opletter.courseevals.common.writeAsJson
import java.io.File

fun copyInstructorStatsWithoutStats(readDir: String, writeDir: String) {
    getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir).mapEachDept { _, _, stats ->
        stats.mapValues { (_, stats) ->
            InstructorStats(
                lastSem = stats.lastSem,
                overallStats = emptyList(),
                courseStats = stats.courseStats.mapValues { emptyList() }
            )
        }
    }.writeToFiles(writeDir)
    val allInstructors = File("$readDir/instructors.json").decodeJson<Map<String, List<Instructor>>>()
    makeFileAndDir("$writeDir/instructors.json").writeAsJson(allInstructors)
}

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: String,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    if (writeSchoolMap) {
        val dirMap = File("jsonData/entries/schools.json").decodeJson<Map<String, School>>()
            .mapValues { (code, school) ->
                val filteredDepts = school.depts.filter { this[code]?.keys?.contains(it) == true }
                school.copy(depts = filteredDepts.toSet())
            }.filterValues { it.depts.isNotEmpty() }

        makeFileAndDir("$writeDir/schools.json").writeAsJson(dirMap)
    }
    forEachDept { school, dept, reports ->
        makeFileAndDir("$writeDir/$school/$dept.json").writeAsJson(reports)
    }
    return this
}