package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.readResource
import java.nio.file.Path

private suspend fun getCourseNamesFromTeachingData(
    statsByProfDir: Path,
    terms: List<Semester.Triple>,
): SchoolDeptsMap<Map<String, String>> {
    val validDepts = statsByProfDir.resolve("schools.json").decodeJson<Map<String, School>>()
        .flatMap { it.value.depts }.toSet()

    return terms.flatMap { getTeachingData(it) }.processTeachingDataByDept { _, dept, entries ->
        if (dept !in validDepts) {
            println("Invalid dept: $dept")
            return@processTeachingDataByDept emptyMap()
        }
        entries.associate { it.courseNumber.drop(3) to it.courseTitle.trim() }
    }
}

// Note: the next 2 functions are similar to ones in usf/ExtraNames.kt & possibly other florida colleges

// CSV downloaded from https://flscns.fldoe.org/PbCourseDescriptions.aspx
private fun getCourseNamesFromCsv(): Map<String, Map<String, String>> {
    return readResource("CourseDescriptions.csv")
        .split("FSU,")
        .drop(1)
        .groupBy { it.take(3) }
        .mapValues { (_, data) ->
            data.associate { line ->
                val name = line.drop(4).substringAfter(",").let {
                    if (it.startsWith("\"")) it.drop(1).substringBefore("\"")
                    else it.substringBefore(",")
                }
                line.drop(4).takeWhile { it != ' ' && it != ',' } to name
            }
        }
}

suspend fun getCompleteCourseNames(
    statsByProfDir: Path,
    terms: List<Semester.Triple>,
    existingCourseNamesDir: Path? = null,
): SchoolDeptsMap<Map<String, String>> {
    // Assuming that all schools/campuses have same course names per code, use csv for all schools
    val fromCsv = getCourseNamesFromCsv()
    val fromTeachingData = getCourseNamesFromTeachingData(statsByProfDir, terms)
    val statsByProf = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(statsByProfDir)

    return fromTeachingData.mapValues { (school, deptMap) ->
        val specificStatsByProf = statsByProf.getValue(school)
        // implementation reused for USF
        (fromCsv + deptMap)
            .filterKeys { it in specificStatsByProf.keys }
            .mapValues { (key, value) ->
                val goodNames = value.filterValues { !it.allLettersUpperCase() }
                val existingGoodNames = existingCourseNamesDir?.resolve(school)?.resolve("$key.json")
                    ?.decodeJsonIfExists<Map<String, String>>()
                    ?.filterValues { !it.allLettersUpperCase() }
                    .orEmpty()
                val combined = fromCsv[key].orEmpty() + value + existingGoodNames + goodNames
                val courseWithData = specificStatsByProf.getValue(key).flatMap { it.value.courseStats.keys }.toSet()

                combined.filterKeys { it in courseWithData }
            }
    }
}

private fun String.allLettersUpperCase() = all { !it.isLetter() || it.isUpperCase() }