package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.decodeFromString
import io.github.opletter.courseevals.common.remote.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.rutgers.remote.SOCSource
import io.github.opletter.courseevals.rutgers.remote.getCoursesOverTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun getDeptNames(writeDir: String?): Map<String, String> {
    return SOCSource.getSOCData().subjects.associate { it.code to it.description }
        .also {
            if (writeDir != null)
                makeFileAndDir("$writeDir/dept-names.json").writeText(Json.encodeToString(it))
        }
}

suspend fun generateCourseNameMappings(
    latestSemester: Semester.Double,
    semestersBack: Int,
    schoolsDir: String,
    writeDir: String?,
    oldDataPath: String?,
): SchoolDeptsMap<Map<String, String>> {
    if (semestersBack < 1) throw IllegalArgumentException("semestersBack must be >= 1")
    return SOCSource.getCoursesOverTime(latestSemester, semestersBack)
        .map { it.courseString to it.title }
        .groupBy { it.first.split(":")[0] } // first split by school
        .mapValues { (school, pairs) ->
            pairs
                .groupBy { it.first.split(":")[1] } // then by dept
                .mapValues { (dept, pairs) ->
                    val new = pairs.associate { it.first.split(":")[2] to it.second }
                    val old = File("$oldDataPath/$school/$dept.json").takeIf { it.exists() }
                        ?.decodeFromString<Map<String, String>>()
                        .orEmpty()
                    (old + new).toSortedMap().toMap()
                }
        }.let {
            // only write course names that will be used
            val schools = File("$schoolsDir/schools.json").decodeFromString<Map<String, School>>()
            it.mapEachDept { school, dept, data ->
                if (schools[school]?.depts?.contains(dept) == true) data else emptyMap()
            }.filterNotEmpty()
        }.also { data ->
            writeDir?.let { data.writeToFiles("$it/course-names", writeSchoolMap = false) }
        }
}

suspend fun getTeachingData(readDir: String, writeDir: String?): SchoolDeptsMap<Map<String, Collection<String>>> {
    val profsByDept = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir)
        .mapEachDept { _, _, map -> map.keys }

    val coursesToProfs = SOCSource.getCoursesOverTime(Semester.Double.valueOf(SemesterType.Fall, 2023), 1)
        .groupBy(
            keySelector = { it.courseString },
            valueTransform = { courseListing ->
                courseListing.sections.flatMap { section ->
                    // instructorsText seems to be updated sooner than instructors?
                    section.instructors.map { it.name } +
                            section.instructorsText.split(";").map { it.trim() }
                }
            }
        ).mapValues { (key, value) ->
            val existingNames = run {
                val (school, dept, _) = key.split(":")
                profsByDept[school]?.get(dept)
            } ?: return@mapValues emptyList()

            value.asSequence().flatten().toSet()
                .mapNotNull { findMatchingName(it, existingNames, key) }
                .toSet().sorted()
        }.filterValues { it.isNotEmpty() }
        .also { println("${it.size} courses with profs, ${it.count { (_, v) -> v.size > 1 }} with 2+ profs") }

    val finalMap = profsByDept.mapEachDept { school, dept, _ ->
        // not very efficient to do this for every dept, but simple and still quick
        val filteredMap = coursesToProfs
            .filterKeys { it.startsWith("$school:$dept:") }
            .mapKeys { it.key.takeLast(3) }
        val profToCourses = filteredMap.flatMap { (course, profs) ->
            profs.map { it to course }
        }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
        filteredMap + profToCourses
    }

    val totalSize = finalMap.values.sumOf { subMap ->
        subMap.values.sumOf { it.size }
    }
    println("${totalSize - coursesToProfs.size} profs with courses")

    return finalMap.also { map ->
        writeDir?.let { map.writeToFiles(it, writeSchoolMap = false) }
    }
}