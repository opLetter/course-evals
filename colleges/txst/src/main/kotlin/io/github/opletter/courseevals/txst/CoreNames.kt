package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.nio.file.Path

// consider also using data from http://mycatalog.txstate.edu/courses/ or raw reports
suspend fun getCourseNames(statsByProfDir: Path, years: IntRange): Map<String, Map<String, String>> {
    val data = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(statsByProfDir).getValue("0")
    return years.pmap { getCourseNamesFromState(it.toString()) }
        .reduce { acc, map ->
            acc.mapValues { (prefix, names) ->
                (names.toMap() + map[prefix]?.toMap().orEmpty()).toList()
            } + map.filterKeys { it !in acc.keys }
        }
        .mapValues { (prefix, nameMap) ->
            val courses = data[prefix]?.flatMap { it.value.courseStats.keys }?.toSet().orEmpty()
            nameMap.filter { it.first in courses }.toMap()
        }
}

private suspend fun getCourseNamesFromState(year: String): Map<String, List<Pair<String, String>>> {
    val payload = Parameters.build {
        append("DefaultYear", year)
        append("CourseYear", year)
        append("FICE", "003615")
        listOf("Rubric", "CourseNum", "CIP", "CourseFunding", "CourseLevel", "courseactive").forEach {
            append(it, "")
        }
        append("outputtype", "Screen")
        append("BAction", "Display Courses")
    }
    return DefaultClient.submitForm("http://www.txhighereddata.org/interactive/UnivCourse/search.cfm", payload)
        .bodyAsText()
        .lines()
        .filter {
            it.trimStart().startsWith("<td valign=\"top\" >") ||
                    it.trimStart().startsWith("<td valign=\"top\"  class=\"ClassB\">")
        }.chunked(4) { info ->
            val parts = info.map { it.substringAfterBefore(">", "<").trim() }
            Triple(parts[0].filter { it != ' ' }, parts[1], parts[3])
        }.groupBy({ it.first }, { it.second to it.third })
}

suspend fun getDeptNames(term: Semester.Triple): Map<String, String> {
    val prefixNames = DefaultClient.submitForm(
        "https://ssb-prod.ec.txstate.edu/PROD/bwckgens.p_proc_term_date",
        Parameters.build {
            append("p_calling_proc", "bwckschd.p_disp_dyn_sched")
            append("p_term", term.toTXSTString())
        }
    ).bodyAsText()
        .lines()
        .dropWhile { it != "<select name=\"sel_subj\" size=\"10\" MULTIPLE ID=\"subj_id\">" }
        .drop(1)
        .takeWhile { it.startsWith("<OPTION") }
        .associate { line ->
            line.substringAfterBefore("VALUE=\"", "\"").filter { it != ' ' } to
                    line.substringAfterBefore(">", "<").replace("&amp;", "&")
        }.filterKeys { it in Prefixes }
        // no longer available, but we still have it in our data
        .plus("CIS" to "Computer Information Systems")
        .plus("CLS" to "Clinical Laboratory Science")
        .plus("QMST" to "Quantitative Methods & Stats")
    check(prefixNames.size == Prefixes.size) { "Missing prefixes: ${Prefixes - prefixNames.keys}" }
    return prefixNames
}