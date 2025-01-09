package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.nio.file.Path

// a bit slow
suspend fun getTeachingDataContent(term: String): String {
    val payload = Parameters.build {
        append("term_in", term)
        append("begin_hh", "0")
        append("begin_mi", "0")
        append("begin_ap", "a")
        append("end_hh", "0")
        append("end_mi", "0")
        append("end_ap", "a")
        listOf("subj", "day", "schd", "insm", "camp", "levl", "sess", "dept", "instr", "ptrm", "attr").forEach {
            append("sel_$it", "dummy")
        }
        listOf("crse", "title", "from_cred", "to_cred").forEach {
            append("sel_$it", "")
        }
        listOf("subj", "schd", "insm", "camp", "levl", "ptrm", "instr", "attr").forEach {
            append("sel_$it", "%")
        }
    }
    return DefaultClient.submitForm("https://ssb-prod.ec.txstate.edu/PROD/bwckschd.p_get_crse_unsec", payload)
        .also { check(it.status == HttpStatusCode.OK) { "Response Not OK: ${it.status}" } }
        .bodyAsText()
}

suspend fun getTeachingProfs(statsByProfDir: Path, term: Semester.Triple): Map<String, Map<String, Set<String>>> {
    return getTeachingDataContent(term.toTXSTString())
        .substringBefore("<table  CLASS=\"datadisplaytable\" summary=\"This is")
        .split("<th CLASS=\"ddtitle\" scope=\"colgroup\" >")
        .drop(1)
        .mapNotNull { data ->
            val courseCode = data
                .substringBefore("</a></th>")
                .substringBeforeLast(" - ")
                .substringAfterLast(" - ")
            val prefix = courseCode.takeWhile { !it.isDigit() }.filter { it != ' ' } // handle "A S 1223"
            val number = courseCode.substringAfterLast(' ')
            val prof = data
                .substringAfterLast("<td CLASS=\"dddefault\">")
                .substringBefore("(")
                .substringBefore("<")
                .replace("\\s+".toRegex(), " ")
                .trim()
            if (prefix !in Prefixes || prof.trim() == "Unassigned Faculty" || prof.isBlank())
                null
            else Triple(prefix, number, prof)
        }.groupBy({ it.first }, { it.third to it.second })
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
    data: List<Pair<String, String>>,
): Map<String, Set<String>> {
    val existingInstructors = statsByProfDir.resolve("0/$subject.json")
        .decodeJson<Map<String, InstructorStats>>()
        .keys

    val teachingInstructors = data.mapNotNull { (name, course) ->
        val first = name.substringBefore(" ").trim()
        val nonLast = name.substringBeforeLast(" ").trim()
        val last = name.substringAfterLast(" ").trim()

        val foundName = existingInstructors.singleOrNull { prof ->
            prof.normalized() == (last + first).normalized()
        } ?: existingInstructors.singleOrNull { prof ->
            prof.normalized(ignoreMiddle = false) == (last + nonLast).normalized()
        } ?: existingInstructors.singleOrNull { prof ->
            prof.normalized() == (last + first.first()).normalized()
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

private fun String.normalized(ignoreMiddle: Boolean = true): String {
    return if (!ignoreMiddle || ',' !in this) {
        this.uppercase().filter { it.isLetter() }
    } else {
        val last = this.substringBefore(',')
        val first = this.substringAfterBefore(", ", " ")
        (last + first).normalized()
    }
}