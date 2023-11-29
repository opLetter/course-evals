package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.readResource
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import java.nio.file.Path

private suspend fun getCourseNamesFromTeachingData(
    statsByProfDir: Path,
    terms: List<Semester.Triple>,
): SchoolDeptsMap<Map<String, String>> {
    val validDepts = statsByProfDir.resolve("schools.json").decodeJson<Map<String, School>>()
        .flatMap { it.value.depts }.toSet()

    return terms.flatMap { term ->
        listOf("Undergraduate", "Graduate", "Law", "Medicine").pmap { type ->
            DefaultClient.get("https://registrar.fsu.edu/class_search/${term.toFSUString()}/$type.pdf")
                .body<ByteArray>()
                .getTeachingData()
        }.flatten()
    }.processTeachingDataByDept { _, dept, entries ->
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
): SchoolDeptsMap<Map<String, String>> {
    val fromCsv = getCourseNamesFromCsv()
    val fromTeachingData = getCourseNamesFromTeachingData(statsByProfDir, terms)

    return fromTeachingData.mapValues { (school, deptMap) ->
        // Assuming that all schools/campuses have same course names per code
        val combined = fromCsv + deptMap.mapValues { (key, value) -> fromCsv[key]?.plus(value) ?: value }

        combined.mapValues inner@{ (key, subMap) ->
            val courseWithData = statsByProfDir.resolve(school).resolve("$key.json")
                .decodeJsonIfExists<Map<String, InstructorStats>>()
                ?.flatMap { it.value.courseStats.keys }
                ?.toSet()
                ?: return@inner emptyMap()
            subMap.filterKeys { it in courseWithData }
        }
    }
}