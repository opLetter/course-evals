package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.rutgers.remote.SOCSource
import io.github.opletter.courseevals.rutgers.remote.getCoursesOverTime
import java.nio.file.Path

suspend fun getDeptNames(): Map<String, String> {
    return SOCSource.getSOCData().subjects.associate { it.code to it.description }
}

suspend fun generateCourseNameMappings(
    latestSemester: Semester.Double,
    semestersBack: Int,
    schoolsDir: Path,
    oldDataPath: Path?,
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
                    val old = oldDataPath?.resolve(school)?.resolve("$dept.json")
                        ?.decodeJsonIfExists<Map<String, String>>()
                        .orEmpty()
                    (old + new).toSortedMap().toMap()
                }
        }.let {
            // only write course names that will be used
            val schools = schoolsDir.resolve("schools.json").decodeJson<Map<String, School>>()
            it.mapEachDept { school, dept, data ->
                if (schools[school]?.depts?.contains(dept) == true) data else emptyMap()
            }.filterNotEmpty()
        }
}

suspend fun getTeachingData(
    readDir: Path,
    term: Semester.Double,
): SchoolDeptsMap<Map<String, Set<String>>> {
    val profsByDept = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir)
        .mapEachDept { _, _, map -> map.keys }

    val coursesToProfs = SOCSource.getCoursesOverTime(term, 1)
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
            } ?: return@mapValues emptySet()

            value.asSequence().flatten().toSet()
                .mapNotNull { findMatchingName(it, existingNames, key) }
                .sorted().toSet()
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
    }.filterNotEmpty()

    val totalSize = finalMap.values.sumOf { subMap ->
        subMap.values.sumOf { it.size }
    }
    println("${totalSize - coursesToProfs.size} profs with courses")

    return finalMap
}