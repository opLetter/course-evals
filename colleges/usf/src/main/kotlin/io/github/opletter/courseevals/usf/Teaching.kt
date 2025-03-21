package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.decodeJsonIfExists
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
            it.trim().startsWith("""<th CLASS="ddheader" scope="col" >Status</th>""") ||
                    it.trim().startsWith("""<th colspan="22" CLASS="ddtitle" scope="colgroup" >""")
        }.map { it.split("""<td CLASS="dddefault">""") }
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
    val statsData = statsByProfDir.resolve("0/$subject.json")
        .decodeJsonIfExists<Map<String, InstructorStats>>()
        ?: return emptyMap()
    val teachingInstructors = data
        .map { it[17].substringBefore(" (") to it[4].substringBefore("<") }
        .filterNot { "To Be Announced" in it.first }
        .mapNotNull { (name, course) ->
            // Name is formatted as "J. Smith"
            val firstInitial = name.first()
            val last = name.drop(3)

            @Suppress("NAME_SHADOWING")
            val potential = statsData.keys.mapNotNull { fullName ->
                val (last, first) = fullName.split(", ")
                if (first.first() == firstInitial) last to fullName else null
            }
            val foundName = potential
                .filter { it.first == last }
                .maxByOrNull { statsData.getValue(it.second).lastSem } // Use most recent active if multiple exact matches
                ?: potential.singleOrNull { it.first.normalized() == last.normalized() }
                ?: potential.filter { "-" in it.first }.run {
                    singleOrNull { it.first.substringBefore("-").normalized() == last.normalized() }
                        ?: singleOrNull { it.first.substringAfter("-").normalized() == last.normalized() }
                }
            foundName?.let { it.second to course }
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