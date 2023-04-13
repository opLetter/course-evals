package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import io.github.opletter.courseevals.common.remote.decodeFromString
import io.github.opletter.courseevals.common.remote.ktorClient
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private suspend fun getCourseNamesFromTeachingData(): SchoolDeptsMap<Map<String, String>> {
    val validDepts = File("jsonData/statsByProf/schools.json").decodeFromString<Map<String, School>>()
        .flatMap { it.value.depts }.toSet()

    return listOf("1", "6", "9").flatMap { term ->
        listOf("Undergraduate", "Graduate", "Law", "Medicine").flatMap { type ->
            ktorClient.get("https://registrar.fsu.edu/class_search/2023-$term/$type.pdf")
                .body<ByteArray>()
                .getTeachingData()
        }
    }.processTeachingDataByDept { _, dept, entries ->
        if (dept !in validDepts) {
            println("Invalid dept: $dept")
            return@processTeachingDataByDept emptyMap()
        }
        entries.associate { it.courseNumber.drop(3) to it.courseTitle }
    }
}

// Note: the next 2 functions are similar to ones in usf/ExtraNames.kt & possibly other florida colleges

// CSV downloaded from https://flscns.fldoe.org/PbCourseDescriptions.aspx
private fun getCourseNamesFromCsv(): Map<String, Map<String, String>> {
    return File("CourseDescriptions.csv").readText()
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
            }.onEach { println(it) }
        }
}

suspend fun getCompleteCourseNames(
    writeDir: String? = "jsonData/extraData/courseNames",
): SchoolDeptsMap<Map<String, String>> {
    val fromCsv = getCourseNamesFromCsv()
    val fromTeachingData = getCourseNamesFromTeachingData()

    return fromTeachingData.mapValues { (school, deptMap) ->
        // Assuming that all schools/campuses have same course names per code
        val combined = fromCsv + deptMap.mapValues { (key, value) -> fromCsv[key]?.plus(value) ?: value }

        combined.mapValues { (key, subMap) ->
            val courseWithData = File("jsonData/statsByProf/$school/$key.json")
                .takeIf { it.exists() }
                ?.let { Json.decodeFromString<Map<String, InstructorStats>>(it.readText()) }
                ?.flatMap { it.value.courseStats.keys }
                ?.toSet()
                ?: emptySet()
            subMap.filterKeys { it in courseWithData }
        }.onEach { (prefix, data) ->
            if (data.isEmpty() || writeDir == null) return@onEach
            makeFileAndDir("$writeDir/$school/$prefix.json")
                .writeText(Json.encodeToString(data.toSortedMap().toMap()))
        }
    }
}