package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.writeAsJson
import java.nio.file.Path

fun copyInstructorStatsWithoutStats(readDir: Path, writeDir: Path) {
    getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir).mapEachDept { _, _, stats ->
        stats.mapValues { (_, stats) ->
            InstructorStats(
                lastSem = stats.lastSem,
                overallStats = emptyList(),
                courseStats = stats.courseStats.mapValues { emptyList() }
            )
        }
    }.writeToFiles(writeDir)
    val allInstructors = readDir.resolve("instructors.json").decodeJson<Map<String, List<Instructor>>>()
    writeDir.resolve("instructors.json").writeAsJson(allInstructors)
}

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: Path,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    if (writeSchoolMap) {
        val dirMap = Path.of("jsonData/entries/schools.json").decodeJson<Map<String, School>>()
            .mapValues { (code, school) ->
                val filteredDepts = school.depts.filter { this[code]?.keys?.contains(it) == true }
                school.copy(depts = filteredDepts.toSet())
            }.filterValues { it.depts.isNotEmpty() }

        writeDir.resolve("schools.json").writeAsJson(dirMap)
    }
    forEachDept { school, dept, reports ->
        writeDir.resolve(school).resolve("$dept.json").writeAsJson(reports)
    }
    return this
}