package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.remote.ktorClient
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

// a bit slow
suspend fun getTeachingDataContent(term: String): String {
    val payload = Parameters.build {
        append("term_in", term)
        listOf(
            "sel_subj", "sel_day", "sel_schd", "sel_insm", "sel_camp", "sel_levl", "sel_sess", "sel_dept",
            "sel_instr", "sel_ptrm", "sel_attr"
        ).forEach { append(it, "dummy") }
        append("sel_subj", "%")
        append("sel_crse", "")
        append("sel_title", "")
        append("sel_schd", "%")
        append("sel_insm", "%")
        append("sel_from_cred", "")
        append("sel_to_cred", "")
        append("sel_camp", "%")
        append("sel_levl", "%")
        append("sel_ptrm", "%")
        append("sel_instr", "%")
        append("sel_attr", "%")
        append("begin_hh", "0")
        append("begin_mi", "0")
        append("begin_ap", "a")
        append("end_hh", "0")
        append("end_mi", "0")
        append("end_ap", "a")
    }
    return ktorClient.submitForm("https://ssb-prod.ec.txstate.edu/PROD/bwckschd.p_get_crse_unsec", payload)
        .bodyAsText()
}

suspend fun getTeachingProfs(
    readDir: String,
    writeDir: String?,
    term: String,
): Map<String, Map<String, Set<String>>> {
    return getTeachingDataContent(term)
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
            else listOf(prefix, number, prof)
        }.groupBy({ it[0] }, { it[2] to it[1] })
        .mapValues { processSubjectData(readDir, it.key, it.value) }
        .onEach { (subject, data) ->
            if (writeDir == null || data.isEmpty()) return@onEach
            makeFileAndDir("$writeDir/0/$subject.json")
                .writeText(Json.encodeToString(data.toSortedMap().toMap()))
        }.also { teachingMap ->
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
    readDir: String,
    subject: String,
    data: List<Pair<String, String>>,
): Map<String, Set<String>> {
    val existingInstructors = File("$readDir/0/$subject.json").readText()
        .let { Json.decodeFromString<Map<String, InstructorStats>>(it) }
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

    return (coursesToProfs + profToCourses).toSortedMap()
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