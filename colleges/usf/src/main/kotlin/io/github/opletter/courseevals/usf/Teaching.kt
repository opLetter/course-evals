package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.decodeJson
import java.nio.file.Path

suspend fun getTeachingProfs(statsByProfDir: Path, term: Semester.Triple): Map<String, Map<String, Set<String>>> {
    val semStr = when (term.type) {
        SemesterType.Spring -> "01"
        SemesterType.Summer -> "05"
        SemesterType.Fall -> "08"
    }
    return getTeachingDataContent("${term.year}$semStr")
        .substringBefore("<table  CLASS=\"datadisplaytable\" summary=\"This is")
        .split("<tr>")
        .asSequence()
        .drop(5)
        .filterNot {
            it.trim().startsWith("<th CLASS=\"ddheader\" scope=\"col\" >Status</th>") ||
                    it.trim().startsWith("<th colspan=\"22\" CLASS=\"ddtitle\" scope=\"colgroup\" >")
        }.map { it.split("<td CLASS=\"dddefault\">") }
        .onEach { if (it.size < 5) println("~$it~") }
        .groupBy { it[3].substringBefore("</td>") }
        .filterKeys { it in Prefixes }
        .mapValues { processSubjectData(statsByProfDir, it.key, it.value) }
        .also { teachingMap ->
            val profCount = teachingMap.values.sumOf { subjectMap ->
                subjectMap.keys.count { it[0].isLetter() }
            }
            val courseWithMultipleProfs = teachingMap.values.sumOf { subjectMap ->
                subjectMap.filterKeys { it[0].isDigit() }.values.count { it.size > 1 }
            }
            println("profCount: $profCount, courseWithMultipleProfs: $courseWithMultipleProfs")
        }
}

private fun processSubjectData(
    statsByProfDir: Path,
    subject: String,
    data: List<List<String>>,
): Map<String, Set<String>> {
    val existingInstructors = statsByProfDir.resolve("0/$subject.json")
        .decodeJson<Map<String, InstructorStats>>()
        .keys
    val teachingInstructors = data
        .map { it[17].substringBefore(" (") to it[4].substringBefore("<") }
        .filterNot { "To Be Announced" in it.first }
        .mapNotNull { (name, course) ->
            val first = name.substringBefore(" ").trim()
            val last = name.substringAfterLast(" ").trim()

            // attempt to handle "Doe, John", "Doe, John A", "Del Doe, John"
            val foundName = existingInstructors.singleOrNull { prof ->
                prof.normalized() == (last + first).normalized()
            } ?: existingInstructors.singleOrNull { prof ->
                val preLast = name.split(" ").filter { it.isNotEmpty() }.dropLast(1)
                prof.normalized() == (preLast.last() + last + first).normalized()
            } ?: existingInstructors.singleOrNull { prof ->
                val preLast = name.split(" ").filter { it.isNotEmpty() }.dropLast(1)
                prof.normalized() == (last + first + preLast.last()).normalized()
            }
            foundName?.let { it to course }
        }

    val coursesToProfs = teachingInstructors
        .groupBy({ it.second }, { it.first })
        .mapValues { it.value.toSortedSet() }

    val profToCourses = coursesToProfs.flatMap { (course, profs) ->
        profs.map { it to course }
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSortedSet() }

    return coursesToProfs + profToCourses
}

private fun String.normalized(): String = this.uppercase().filter { it.isLetter() }