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
    return getTeachingData("${term.year}$semStr")
        .groupBy { it.subject }
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
    teachingData: List<TeachingData>,
): Map<String, Set<String>> {
    val statsData = statsByProfDir.resolve("0/$subject.json")
        .decodeJsonIfExists<Map<String, InstructorStats>>()
        ?: return emptyMap()
    val teachingInstructors = teachingData.mapNotNull { data ->
        // Name is formatted as "Smith, J."
        val firstInitial = data.prof.dropLast(1).last()
        val last = data.prof.substringBefore(',')

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
        foundName?.let { it.second to data.course }
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